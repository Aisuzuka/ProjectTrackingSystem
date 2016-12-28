package com.se.server.service;

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
		User user = userRepository.findOne(userId);
		Project project = projectRepository.findOne(projectId);
		if (isNull(user))
			return ErrorCode.UserNull;
		else if (isNull(project))
			return ErrorCode.ProjectNull;
		else if (isRelationalUser(user, project)) {
			Set<MemberGroup> list = project.getMemberGroup();
			for (MemberGroup member : list) {
				if (user.getId() == member.getUser().getId()) {
					if (isParty(user, member)) {
						if (isAgree(request)) {
							member.setJoined(true);
							member = memberGroupRepository.save(member);
						} else {
							return deleteMember(userId, projectId, userId);
						}
					} else {
						member.setRole(request.getMember().getRole());
						member = memberGroupRepository.save(member);
						return ErrorCode.Correct;
					}
				}
			}
			return ErrorCode.Correct;
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
		else if (isProjectManager(user, project)) {
			Set<MemberGroup> listP = project.getMemberGroup();
			Set<MemberGroup> listU = delUser.getJoinMemberGroups();
			MemberGroup member = new MemberGroup();
			for (MemberGroup item : listP) {
				if (delUser.getId() == item.getUser().getId()) {
					member = item;
				}
			}
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

	private boolean isParty(User user, MemberGroup member) {
		return user.getId() == member.getUser().getId();
	}

	private boolean isAgree(MemberDetailRequest request) {
		return request.getMember().getIsJoined().equals("1");
	}
	
	private  void replaceIssueRelation(Project project, User user) {
		// Issue's user relation will be replaced to project manager.
		Set<Issue> list = user.getResponsibleIssue();
		for (Issue item : list) {
			if (item.getReporterId().getId() == user.getId()) {
				item.setReporterId(project.getManager());
			}
			if (item.getPersonInChargeId().getId() == user.getId()) {
				item.setPersonInChargeId(project.getManager());
			}
			item = issueRepository.save(item);
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
}
