package com.se.server.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.se.api.data.IssueData;
import com.se.api.request.IssueRequest;
import com.se.api.response.IssueItemResponse;
import com.se.api.response.IssueListResponse;
import com.se.server.entity.Issue;
import com.se.server.entity.IssueGroup;
import com.se.server.entity.Project;
import com.se.server.entity.User;
import com.se.server.repository.IssueGroupRepository;
import com.se.server.repository.IssueRepository;
import com.se.server.repository.ProjectRepository;
import com.se.server.repository.UserRepository;

@RestController
@RequestMapping(value = "/api")
@Transactional("jpaTransactionManager")
public class IssueService {
	@Autowired
	IssueGroupRepository issueGroupRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	ProjectRepository projectRepository;
	@Autowired
	IssueRepository issueRepository;

	@RequestMapping(value = "/issues/{userId}/{projectId}", method = RequestMethod.POST)
	public IssueItemResponse createIssue(@PathVariable int userId, @PathVariable int projectId,
			@RequestBody IssueRequest request) {
		IssueItemResponse response = new IssueItemResponse();
		User user = userRepository.findOne(userId);
		Project project = projectRepository.findOne(projectId);
		// User personInCharge =
		// userRepository.findOne(request.getPersonInChargeId());
		if (isNull(user))
			response.setState(-1);
		else if (isNull(project))
			response.setState(-1);
		// else if (isNull(personInCharge))
		// response.setState(-1);
		else {
			IssueGroup issueGroup = new IssueGroup();
			issueGroup.setProject(project);
			issueGroup = issueGroupRepository.save(issueGroup);

			Issue issue = new Issue();
			issue.setDescription(request.getDescription());
			issue.setFinishTime(null);
			issue.setIssueGroup(issueGroup);
			issue.setPersonInChargeId(project.getManager());
			issue.setPriority(request.getPriovify());
			issue.setReporterId(user);
			issue.setReportTime(new Date());
			issue.setServerity(request.getServerity());
			issue.setState(request.getState());
			issue.setTitle(request.getTitle());
			issue = issueRepository.save(issue);

			issueGroup = addIssue2IssueGroup(issue, issueGroup);
			project = addIssueGroup2Project(issueGroup, project);

			IssueData model = generateIssueModel(issue);
			response.setState(0);
			response.setIssue(model);
		}
		return response;

	}

	@RequestMapping(value = "/issues/{userId}/{issueId}", method = RequestMethod.GET)
	public IssueItemResponse getIssueInfo(@PathVariable int userId, @PathVariable int issueId) {
		IssueItemResponse response = new IssueItemResponse();
		Issue issue = issueRepository.findOne(issueId);
		User user = userRepository.findOne(userId);
		if (isNull(user))
			response.setState(-1);
		else if (isNull(issue))
			response.setState(-1);
		else if (user.getId() == issue.getIssueGroup().getProject().getManager().getId()) {
			IssueData model = generateIssueModel(issue);
			response.setIssue(model);
			response.setState(0);
		} else {
			response.setState(-1);
		}
		return response;
	}

	@RequestMapping(value = "/issues/list/{userId}", method = RequestMethod.GET)
	public IssueListResponse getIssueListByUserId(@PathVariable int userId) {
		IssueListResponse response = new IssueListResponse();
		User user = userRepository.findOne(userId);
		if (isNull(user))
			response.setState(-1);
		else {
			Set<Issue> list = user.getResponsibleIssue();
			list.addAll(user.getHandleIssue());
			List<IssueData> listModel = generateIssueList(list);
			response.setList(listModel);
			response.setState(0);
		}
		return response;
	}

	@RequestMapping(value = "/issues/list/{userId}/{projectId}", method = RequestMethod.GET)
	public IssueListResponse getIssueListByProjectId(@PathVariable int userId, @PathVariable int projectId) {
		IssueListResponse response = new IssueListResponse();
		User user = userRepository.findOne(userId);
		Project project = projectRepository.findOne(projectId);
		if (isNull(user))
			response.setState(-1);
		else if (isNull(project))
			response.setState(-1);
		else if (project.getManager().getId() == user.getId()) {
			Set<IssueGroup> listGroup = project.getIssueGroup();
			Set<Issue> list = new HashSet<>();
			for (IssueGroup group : listGroup) {
				list.addAll(group.getIssues());
			}
			List<IssueData> listModel = generateIssueList(list);
			response.setList(listModel);
			response.setState(0);
		} else {
			response.setState(-1);
		}
		return response;
	}

	// @RequestMapping(value = "/issues/{userId}", method = RequestMethod.GET)
	// public void getAllIssueList(@PathVariable int userId) {
	//
	// }

	@RequestMapping(value = "/issues/{userId}/{issueId}", method = RequestMethod.PUT)
	public int updateIssue(@PathVariable int userId, @PathVariable int issueId, @RequestBody IssueRequest request) {
		Issue issue = issueRepository.findOne(issueId);
		User user = userRepository.findOne(userId);
		User projectManager = userRepository.findOne(request.getPersonInChargeId());
		if (isNull(issue))
			return -1;
		else if (isNull(user))
			return -1;
		else if (isNull(projectManager))
			return -1;
		else if (isPersonInCharge(user, issue)) {
			issue.setFinishTime(new Date());
			issue = issueRepository.save(issue);

			IssueGroup issueGroup = issueGroupRepository.findOne(issue.getIssueGroup().getId());
			Project project = projectRepository.findOne(issueGroup.getProject().getId());
			Issue newIssue = new Issue();
			newIssue.setDescription(request.getDescription());
			newIssue.setFinishTime(null);
			newIssue.setIssueGroup(issueGroup);
			User personInCharge = userRepository.findOne(request.getPersonInChargeId());
			if (isNull(personInCharge)) {
				return -1;
			}
			newIssue.setPersonInChargeId(personInCharge);
			newIssue.setPriority(request.getPriovify());
			newIssue.setReporterId(user);
			newIssue.setReportTime(new Date());
			newIssue.setServerity(request.getServerity());
			newIssue.setState(request.getState());
			newIssue.setTitle(request.getTitle());
			newIssue = issueRepository.save(newIssue);

			issueGroup = addIssue2IssueGroup(newIssue, issueGroup);
			project = addIssueGroup2Project(issueGroup, project);
			return 0;
		} else if (isReporter(user, issue) || isProjectManager(user, issue)) {
			issue.setDescription(request.getDescription());
			issue.setPriority(request.getPriovify());
			issue.setServerity(request.getServerity());
			issue.setState(request.getState());
			issue.setTitle(request.getTitle());
			issue.setPersonInChargeId(projectManager);
			issue = issueRepository.save(issue);
			return 0;
		} else {
			return -1;
		}
	}

	private boolean isProjectManager(User user, Issue issue) {
		return user.getId() == issue.getIssueGroup().getProject().getManager().getId();
	}

	private boolean isPersonInCharge(User user, Issue issue) {
		return user.getId() == issue.getPersonInChargeId().getId();
	}

	private Project addIssueGroup2Project(IssueGroup issueGroup, Project project) {
		Set<IssueGroup> list = project.getIssueGroup();
		list.add(issueGroup);
		project.setIssueGroup(list);
		return projectRepository.save(project);
	}

	private IssueGroup addIssue2IssueGroup(Issue issue, IssueGroup issueGroup) {
		Set<Issue> list = issueGroupRepository.findOne(issueGroup.getId()).getIssues();
		list.add(issue);
		issueGroup.setIssues(list);
		issueGroup.setLastIssueId(issue.getId());
		return issueGroupRepository.save(issueGroup);
	}

	private List<IssueData> generateIssueList(Set<Issue> list) {
		List<IssueData> listModel = new ArrayList<IssueData>();
		for (Issue issue : list) {
			IssueData model = generateIssueModel(issue);
			listModel.add(model);
		}
		return listModel;
	}

	private IssueData generateIssueModel(Issue issue) {
		IssueData model = new IssueData();
		model.setIssueId(issue.getId());
		model.setIssueGroupId(issue.getIssueGroup().getId());
		model.setProjectId(issue.getIssueGroup().getProject().getId());
		model.setDescription(issue.getDescription());
		model.setFinishTime(issue.getFinishTime());
		model.setPersonInChargeId(issue.getPersonInChargeId().getId());
		model.setPriority(issue.getPriority());
		model.setReporterId(issue.getReporterId().getId());
		model.setReportTime(issue.getReportTime());
		model.setServerity(issue.getServerity());
		model.setState(issue.getState());
		model.setTitle(issue.getTitle());
		return model;
	}

	private boolean isReporter(User user, Issue issue) {
		return user.getId() == issue.getReporterId().getId();
	}

	private boolean isNull(Object object) {
		return object == null;
	}
}
