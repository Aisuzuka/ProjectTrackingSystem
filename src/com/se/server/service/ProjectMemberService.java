package com.se.server.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.se.api.data.ErrorCode;
import com.se.api.data.MemberData;
import com.se.api.request.MemberCreateRequest;
import com.se.api.request.MemberDetailRequest;
import com.se.api.response.MemberItemResponse;
import com.se.api.response.MemberListResponse;
import com.se.server.entity.Issue;
import com.se.server.entity.IssueGroup;
import com.se.server.entity.MemberGroup;
import com.se.server.entity.Project;
import com.se.server.entity.User;
import com.se.server.repository.IssueGroupRepository;
import com.se.server.repository.IssueRepository;
import com.se.server.repository.MemberGroupRepository;
import com.se.server.repository.ProjectRepository;
import com.se.server.repository.UserRepository;
import com.se.tool.TerminalToHtml;

@RestController
@RequestMapping(value = "/api")
@Transactional("jpaTransactionManager")
public class ProjectMemberService {
	@Autowired
	IssueGroupRepository issueGroupRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	ProjectRepository projectRepository;
	@Autowired
	IssueRepository issueRepository;
	@Autowired
	MemberGroupRepository memberGroupRepository;
	@Autowired
	EmailService emailService;

	@RequestMapping(value = "/members/{userId}/{projectId}", method = RequestMethod.POST)
	public MemberItemResponse createMember(@PathVariable int userId, @PathVariable int projectId,
			@RequestBody MemberCreateRequest request) {
		MemberItemResponse response = new MemberItemResponse();
		User customer = userRepository.findOne(request.getUserId());
		User host = userRepository.findOne(userId);
		Project project = projectRepository.findOne(projectId);
		if (isNull(customer))
			response.setState(ErrorCode.CustomerNull);
		else if (isNull(host))
			response.setState(ErrorCode.UserNull);
		else if (isNull(project))
			response.setState(ErrorCode.ProjectNull);
		else if (isSystemManager(customer))
			response.setState(ErrorCode.SMCantInvited);
		else if (isIllegalRole(request.getRole()))
			response.setState(ErrorCode.CantSetIllegalRole);
		else if (isRelationalUser(customer, project))
			response.setState(ErrorCode.UserIsInProject);
		else if (isProjectManager(host, project)) {
			MemberGroup member = new MemberGroup();
			member.setUser(customer);
			member.setJoined(false);
			member.setProject(project);
			member.setRole(request.getRole());
			member = memberGroupRepository.save(member);

			Set<MemberGroup> list = project.getMemberGroup();
			list.add(member);
			project.setMemberGroup(list);
			project = projectRepository.save(project);

			list = customer.getJoinMemberGroups();
			list.add(member);
			customer.setJoinMemberGroups(list);
			customer = userRepository.save(customer);

			MemberData model = new MemberData();
			model.setIsJoined(member.isJoined() ? "1" : "0");
			model.setRole(member.getRole());
			model.setUserId(member.getUser().getId());

			SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd a hh:mm");
			String message = new TerminalToHtml().append(customer.getName()).append("你好：").enter().append("專案管理員")
					.append(host.getName()).setBold(true).setColor(0, 0, 255).append("邀請你加入專案")
					.append(project.getName()).setBold(true).setColor(0, 0, 255).enter().append("以下為專案內容").enter()
					.enter().append("標題：").append(project.getName()).enter().append("描述：")
					.append(project.getDescription()).enter().append("管理員：").append(host.getName()).enter()
					.append("創立時間：").append(sdFormat.format(project.getTimeStamp())).enter().enter()
					.append("請記得登入系統回覆邀請").setBold(true).enter().append("祝你有美好的一天").toHtml();
			emailService.generateAndSendEmail(customer.getEmailAddress(),
					host.getName() + "邀請你加入專案" + project.getName(), message);
			response.setMember(model);
			response.setState(ErrorCode.Correct);
		} else {
			response.setState(ErrorCode.NotProjectManager);
		}
		return response;
	}

	@RequestMapping(value = "/members/list/{userId}/{projectId}", method = RequestMethod.GET)
	public MemberListResponse getMemberByProjectId(@PathVariable int userId, @PathVariable int projectId) {
		MemberListResponse response = new MemberListResponse();
		User user = userRepository.findOne(userId);
		Project project = projectRepository.findOne(projectId);
		if (isNull(user))
			response.setState(ErrorCode.UserNull);
		else if (isNull(project))
			response.setState(ErrorCode.ProjectNull);
		else if (isRelationalUser(user, project)) {
			List<MemberData> listModel = new ArrayList<MemberData>();
			Set<MemberGroup> list = project.getMemberGroup();
			for (MemberGroup member : list) {
				MemberData model = new MemberData();
				model.setIsJoined(member.isJoined() ? "1" : "0");
				model.setRole(member.getRole());
				model.setUserId(member.getUser().getId());
				listModel.add(model);
			}
			response.setMember(listModel);
			response.setState(ErrorCode.Correct);
		} else {
			response.setState(ErrorCode.NotMember);
		}
		return response;
	}

	@RequestMapping(value = "/members/put/{userId}/{projectId}", method = RequestMethod.POST)
	public int updateInfo(@PathVariable int userId, @PathVariable int projectId,
			@RequestBody MemberDetailRequest request) {
		User host = userRepository.findOne(userId);
		User customer = userRepository.findOne(request.getMember().getUserId());
		Project project = projectRepository.findOne(projectId);
		if (isNull(host))
			return ErrorCode.UserNull;
		else if (isNull(customer))
			return ErrorCode.CustomerNull;
		else if (isNull(project))
			return ErrorCode.ProjectNull;
		else if (isSystemManager(customer))
			return ErrorCode.NotSystemManager;
		else if (isProjectManager(customer, project))
			return ErrorCode.CantChangeProjectManager;
		else if (isIllegalRole(request.getMember().getRole()))
			return ErrorCode.CantSetIllegalRole;
		else if (isRelationalUser(host, project)) {
			Set<MemberGroup> list = project.getMemberGroup();
			for (MemberGroup member : list) {
				if (customer.getId() == member.getUser().getId()) {
					if (isParty(host, member)) {
						if (isReplaied(member))
							return ErrorCode.AlreadyReplaied;
						User projectManager = project.getManager();
						String message = new TerminalToHtml().append(projectManager.getName()).append("你好：").enter()
								.append("你邀請的成員").append(host.getName()).setBold(true).setColor(0, 0, 255)
								.append((isAgree(request) ? "同意" : "拒絕") + "加入專案").append(project.getName())
								.setBold(true).setColor(0, 0, 255).enter().enter().append("祝你有美好的一天").toHtml();
						emailService.generateAndSendEmail(projectManager.getEmailAddress(),
								host.getName() + "已回覆你的專案邀請" + project.getName(), message);
						if (isAgree(request)) {
							member.setJoined(true);
							member = memberGroupRepository.save(member);
							return ErrorCode.Correct;
						} else {
							// return deleteMember(project.getManager().getId(),
							// projectId, userId);
							return rejectInvite(customer, project);
						}
					} else {
						member.setRole(request.getMember().getRole());
						member = memberGroupRepository.save(member);
						return ErrorCode.Correct;
					}
				}
			}
			return ErrorCode.NotMember;
		} else {
			return ErrorCode.NotMember;
		}
	}

	// @RequestMapping(value = "/members/{userId}/{projectId}/{memberId}",
	// method = RequestMethod.PUT)
	// public void updateUserPermissionByUserId(@PathVariable int userId,
	// @PathVariable int projectId,
	// @PathVariable int memberId, @RequestBody String member) {
	//
	// }
	
	@RequestMapping(value = "/members/delete/{userId}/{projectId}/{delUserId}", method = RequestMethod.POST)
	public int deleteMember(@PathVariable int userId, @PathVariable int projectId, @PathVariable int delUserId) {
		User user = userRepository.findOne(userId);
		Project project = projectRepository.findOne(projectId);
		User delUser = userRepository.findOne(delUserId);
		if (isNull(user))
			return ErrorCode.UserNull;
		else if (isNull(project))
			return ErrorCode.ProjectNull;
		else if (isNull(delUser))
			return ErrorCode.CustomerNull;
		else if (isProjectManager(delUser, project))
			return ErrorCode.PMCantRemove;
		else if (!isRelationalUser(delUser, project))
			return ErrorCode.NotMember;
		else if (isProjectManager(user, project)) {
			Set<MemberGroup> listP = project.getMemberGroup();
			Set<MemberGroup> listU = delUser.getJoinMemberGroups();
			MemberGroup member = null;
			for (MemberGroup item : listP) {
				if (delUser.getId() == item.getUser().getId()) {
					member = item;
				}
			}
			if (isNull(member))
				return ErrorCode.UserIsNotInProject;
			else {
				String message = new TerminalToHtml().append(delUser.getName()).append("你好：").enter().append("專案")
						.append(project.getName()).setBold(true).setColor(0, 0, 255).append("的專案管理員已解除你的職務").enter()
						.enter().append("祝你有美好的一天").toHtml();
				emailService.generateAndSendEmail(delUser.getEmailAddress(), "專案" + project.getName() + "的職務已被解除",
						message);
				delUser.getJoinMemberGroups().remove(member);
				project.getMemberGroup().remove(member);
				member.setProject(null);
				member.setUser(null);

				replaceIssueRelation(project, delUser);

				project.setMemberGroup(listP);
				project = projectRepository.save(project);

				delUser.setJoinMemberGroups(listU);
				delUser = userRepository.save(delUser);

				memberGroupRepository.delete(member);
				return ErrorCode.Correct;
			}
		} else {
			return ErrorCode.NotProjectManager;
		}
	}

	// @RequestMapping(value = "/members/{userId}/{projectId}", method =
	// RequestMethod.PUT)
	// public void replayProjectInvite(@PathVariable int userId, @PathVariable
	// int projectId,
	// @RequestBody boolean isAccepted) {
	//
	// }

	private boolean isReplaied(MemberGroup member) {
		return member.isJoined();
	}

	private boolean isParty(User user, MemberGroup member) {
		return user.getId() == member.getUser().getId();
	}

	private boolean isAgree(MemberDetailRequest request) {
		return request.getMember().getIsJoined().equals("1");
	}

	private void replaceIssueRelation(Project project, User delUser) {
		// Issue's user relation will be replaced to project manager.
		User manager = userRepository.findOne(project.getManager().getId());
		IssueGroup[] groupList = project.getIssueGroup().toArray(new IssueGroup[project.getIssueGroup().size()]);
		Set<Issue> projectIssue = manager.getResponsibleIssue();
		for (int i = 0; i < groupList.length; i++) {
			IssueGroup groupItem = groupList[i];
			Issue[] list = groupItem.getIssues().toArray(new Issue[groupItem.getIssues().size()]);
			for (int j = 0; j < list.length; j++) {
				Issue item = list[j];
				if (item.getReporterId().getId() == delUser.getId()) {
					item.setReporterId(project.getManager());
					// projectIssue.add(item);
					// manager = userRepository.save(manager);
					item = issueRepository.save(item);
				}
				if (item.getPersonInChargeId().getId() == delUser.getId()) {
					item.setPersonInChargeId(project.getManager());
					// projectIssue.add(item);
					// manager = userRepository.save(manager);
					item = issueRepository.save(item);
				}
			}
		}
	}

	// private Set<MemberGroup> removeRelation(MemberGroup member,
	// Set<MemberGroup> list) {
	// list.remove(member);
	// return list;
	// }

	private boolean isRelationalUser(User user, Project project) {
		Set<MemberGroup> list = project.getMemberGroup();
		for (MemberGroup member : list) {
			if (user.getId() == member.getUser().getId()) {
				return true;
			}
		}
		return false;
	}

	private boolean isProjectManager(User user, Project project) {
		if (user.getId() == project.getManager().getId()) {
			return true;
		}
		return false;
	}

	private boolean isNull(Object object) {
		return object == null;
	}

	private boolean isSystemManager(User user) {
		return user.getRole().equals("SystemManager");
	}

	private boolean isIllegalRole(String role) {
		return role.equals("ProjectManager");
	}
	
	private int rejectInvite(User customer, Project project) {
		Set<MemberGroup> listP = project.getMemberGroup();
		Set<MemberGroup> listU = customer.getJoinMemberGroups();
		MemberGroup member = null;
		for (MemberGroup item : listP) {
			if (customer.getId() == item.getUser().getId()) {
				member = item;
			}
		}
		customer.getJoinMemberGroups().remove(member);
		project.getMemberGroup().remove(member);
		member.setProject(null);
		member.setUser(null);

		replaceIssueRelation(project, customer);

		project.setMemberGroup(listP);
		project = projectRepository.save(project);

		customer.setJoinMemberGroups(listU);
		customer = userRepository.save(customer);

		memberGroupRepository.delete(member);
		return ErrorCode.Correct;
	}

}
