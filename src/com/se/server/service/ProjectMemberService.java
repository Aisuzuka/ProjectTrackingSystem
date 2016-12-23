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

import com.se.api.data.MemberData;
import com.se.api.request.MemberCreateRequest;
import com.se.api.request.MemberDetailRequest;
import com.se.api.response.MemberItemResponse;
import com.se.api.response.MemberListResponse;
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
		if (host.getId() == project.getManager().getId()) {
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
			model.setJoined(member.getJoined());
			model.setRole(member.getRole());
			model.setUserId(member.getUser().getId());

			response.setMember(model);
			response.setState(0);
		} else {
			response.setMember(new MemberData());
			response.setState(-1);
		}
		return response;
	}

	@RequestMapping(value = "/members/list/{userId}/{projectId}", method = RequestMethod.GET)
	public MemberListResponse getMemberByProjectId(@PathVariable int userId, @PathVariable int projectId) {
		MemberListResponse response = new MemberListResponse();
		User user = userRepository.findOne(userId);
		Project project = projectRepository.findOne(projectId);
		if (isRelationalUser(user, project)) {
			List<MemberData> listModel = new ArrayList<MemberData>();
			Set<MemberGroup> list = project.getMemberGroup();
			for (MemberGroup member : list) {
				MemberData model = new MemberData();
				model.setJoined(member.getJoined());
				model.setRole(member.getRole());
				model.setUserId(member.getUser().getId());
				listModel.add(model);
			}
			response.setMember(listModel);
			response.setState(0);
		} else {
			response.setMember(new ArrayList<MemberData>());
			response.setState(-1);
		}
		return response;
	}

	@RequestMapping(value = "/members/{userId}/{projectId}", method = RequestMethod.PUT)
	public int updateInfo(@PathVariable int userId, @PathVariable int projectId,
			@RequestBody MemberDetailRequest request) {
		User user = userRepository.findOne(userId);
		Project project = projectRepository.findOne(projectId);
		if (isRelationalUser(user, project)) {
			Set<MemberGroup> list = project.getMemberGroup();
			for (MemberGroup member : list) {
				if (request.getMember().getUserId() == member.getId()) {
					list.remove(member);
					member.setJoined(request.getMember().getJoined());
					member.setRole(request.getMember().getRole());
					list.add(member);
				}
			}
			project.setMemberGroup(list);
			project = projectRepository.save(project);
			return 0;
		} else {
			return -1;
		}
	}

	// @RequestMapping(value = "/members/{userId}/{projectId}/{memberId}",
	// method = RequestMethod.PUT)
	// public void updateUserPermissionByUserId(@PathVariable int userId,
	// @PathVariable int projectId,
	// @PathVariable int memberId, @RequestBody String member) {
	//
	// }

	@RequestMapping(value = "/members/{userId}/{delUserId}", method = RequestMethod.DELETE)
	public int deleteMember(@PathVariable int userId, @PathVariable int projectId, @PathVariable int delUserId) {
		User user = userRepository.findOne(userId);
		Project project = projectRepository.findOne(projectId);
		if (isProjectManager(user, project)) {
			User delUser = userRepository.findOne(delUserId);
			Set<MemberGroup> listP = project.getMemberGroup();
			Set<MemberGroup> listU = delUser.getJoinMemberGroups();
			MemberGroup member = new MemberGroup();
			for (MemberGroup item : listU) {
				if (delUser.getId() == item.getUser().getId()) {
					member = item;
					member.setUser(null);
					member.setProject(null);
				}
			}
			listP = removeRelation(delUser, listP);
			listU = removeRelation(delUser, listU);
			
			project.setMemberGroup(listP);
			project = projectRepository.save(project);
			
			delUser.setJoinMemberGroups(listU);
			delUser = userRepository.save(delUser);
			
			memberGroupRepository.delete(member);
			return 0;
		} else {
			return -1;
		}
	}

	// @RequestMapping(value = "/members/{userId}/{projectId}", method =
	// RequestMethod.PUT)
	// public void replayProjectInvite(@PathVariable int userId, @PathVariable
	// int projectId,
	// @RequestBody boolean isAccepted) {
	//
	// }

	private Set<MemberGroup> removeRelation(User delUser, Set<MemberGroup> list) {
		for (MemberGroup item : list) {
			if (delUser.getId() == item.getUser().getId()) {
				list.remove(item);
				item.setProject(null);
				item.setUser(null);
			}
		}
		return list;
	}

	private boolean isRelationalUser(User user, Project project) {
		Set<MemberGroup> list = project.getMemberGroup();
		if (user.getId() == project.getManager().getId()) {
			return true;
		}
		for (MemberGroup member : list) {
			if (user.getId() == member.getId()) {
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
}
