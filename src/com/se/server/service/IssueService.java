package com.se.server.service;

import java.text.SimpleDateFormat;
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

import com.se.api.data.ErrorCode;
import com.se.api.data.IssueData;
import com.se.api.request.IssueRequest;
import com.se.api.response.IssueItemResponse;
import com.se.api.response.IssueListResponse;
import com.se.api.response.IssueResponse;
import com.se.server.entity.Issue;
import com.se.server.entity.IssueGroup;
import com.se.server.entity.MemberGroup;
import com.se.server.entity.Project;
import com.se.server.entity.User;
import com.se.server.repository.IssueGroupRepository;
import com.se.server.repository.IssueRepository;
import com.se.server.repository.ProjectRepository;
import com.se.server.repository.UserRepository;
import com.se.tool.TerminalToHtml;

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
	@Autowired
	EmailService emailService;

	@RequestMapping(value = "/issues/{userId}/{projectId}", method = RequestMethod.POST)
	public IssueItemResponse createIssue(@PathVariable int userId, @PathVariable int projectId,
			@RequestBody IssueRequest request) {
		IssueItemResponse response = new IssueItemResponse();
		User user = userRepository.findOne(userId);
		Project project = projectRepository.findOne(projectId);
		// User personInCharge =
		// userRepository.findOne(request.getPersonInChargeId());
		if (isNull(user))
			response.setState(ErrorCode.UserNull);
		else if (isNull(project))
			response.setState(ErrorCode.ProjectNull);
		else if (!isRelationalUser(user, project))
			response.setState(ErrorCode.UserIsNotInProject);
		else {
			IssueGroup issueGroup = new IssueGroup();
			issueGroup.setProject(project);
			issueGroup = issueGroupRepository.save(issueGroup);

			Issue issue = new Issue();
			issue.setDescription(request.getDescription());
			issue.setFinishTime(null);
			issue.setIssueGroup(issueGroup);
			issue.setPersonInChargeId(project.getManager());
			issue.setPriority(request.getPriority());
			issue.setReporterId(user);
			issue.setReportTime(new Date());
			issue.setServerity(request.getServerity());
			issue.setState(request.getState());
			issue.setTitle(request.getTitle());
			issue = issueRepository.save(issue);

			issueGroup = addIssue2IssueGroup(issue, issueGroup);
			project = addIssueGroup2Project(issueGroup, project);

			IssueData model = generateIssueModel(issue);
			User projectManager = project.getManager();
			SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd a hh:mm");
			String message = new TerminalToHtml().append(projectManager.getName()).append("你好：").enter().append("專案")
					.append(project.getName()).setBold(true).setColor(0, 0, 255).append("有一個新的議題被回報").enter()
					.append("以下為議題內容").enter().enter().append("標題：").append(issue.getTitle()).enter().append("描述：")
					.append(issue.getDescription()).enter().append("回報人：").append(user.getName()).enter()
					.append("指派時間：").append(sdFormat.format(issue.getReportTime())).enter().enter()
					.append("請記得登入系統完成議題指派").setBold(true).enter().append("祝你有美好的一天").toHtml();
			emailService.generateAndSendEmail(projectManager.getEmailAddress(), project.getName() + "有新的議題被回報",
					message);
			response.setState(ErrorCode.Correct);
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
			response.setState(ErrorCode.UserNull);
		else if (isNull(issue))
			response.setState(ErrorCode.IssueNull);
		else if (user.getId() == issue.getIssueGroup().getProject().getManager().getId()) {
			IssueData model = generateIssueModel(issue);
			response.setIssue(model);
			response.setState(ErrorCode.Correct);
		} else {
			response.setState(ErrorCode.NotProjectManager);
		}
		return response;
	}

	@RequestMapping(value = "/issues/list/{userId}", method = RequestMethod.GET)
	public IssueListResponse getIssueListByUserId(@PathVariable int userId) {
		IssueListResponse response = new IssueListResponse();
		User user = userRepository.findOne(userId);
		if (isNull(user))
			response.setState(ErrorCode.UserNull);
		else {
			Set<Issue> list = user.getResponsibleIssue();
			list.addAll(user.getHandleIssue());
			List<IssueData> listModel = generateIssueList(list);
			response.setList(listModel);
			response.setState(ErrorCode.Correct);
		}
		return response;
	}

	@RequestMapping(value = "/issues/list/{userId}/{projectId}", method = RequestMethod.GET)
	public IssueListResponse getIssueListByProjectId(@PathVariable int userId, @PathVariable int projectId) {
		IssueListResponse response = new IssueListResponse();
		User user = userRepository.findOne(userId);
		Project project = projectRepository.findOne(projectId);
		if (isNull(user))
			response.setState(ErrorCode.UserNull);
		else if (isNull(project))
			response.setState(ErrorCode.ProjectNull);
		else if (isRelationalUser(user, project)) {
			Set<IssueGroup> listGroup = project.getIssueGroup();
			Set<Issue> list = new HashSet<>();
			for (IssueGroup group : listGroup) {
				list.addAll(group.getIssues());
			}
			List<IssueData> listModel = generateIssueList(list);
			response.setList(listModel);
			response.setState(ErrorCode.Correct);
		} else {
			response.setState(ErrorCode.NotMember);
		}
		return response;
	}

	@RequestMapping(value = "/issues/{userId}", method = RequestMethod.GET)
	public IssueListResponse getAllIssueList(@PathVariable int userId) {
		IssueListResponse response = new IssueListResponse();
		User user = userRepository.findOne(userId);
		if (!isSystemManager(user))
			response.setState(ErrorCode.NotSystemManager);
		else {
			Iterable<Issue> listGroup = issueRepository.findAll();
			Set<Issue> list = new HashSet<>();
			for (Issue item : listGroup) {
				list.add(item);
			}
			List<IssueData> listModel = generateIssueList(list);
			response.setList(listModel);
			response.setState(ErrorCode.Correct);
		}
		return response;
	}

	@RequestMapping(value = "/issues/put/{userId}/{issueId}", method = RequestMethod.POST)
	public IssueResponse updateIssue(@PathVariable int userId, @PathVariable int issueId,
			@RequestBody IssueRequest request) {
		Issue issue = issueRepository.findOne(issueId);
		User user = userRepository.findOne(userId);
		IssueResponse response = new IssueResponse();
		if (isNull(issue))
			response.setState(ErrorCode.IssueNull);
		else if (isNull(user))
			response.setState(ErrorCode.UserNull);
		else if (isPersonInCharge(user, issue)) {
			IssueGroup issueGroup = issueGroupRepository.findOne(issue.getIssueGroup().getId());
			Project project = projectRepository.findOne(issueGroup.getProject().getId());
			Issue newIssue = new Issue();
			User personInCharge = userRepository.findOne(request.getPersonInChargeId());
			if (isNull(personInCharge))
				response.setState(ErrorCode.PersonInChargeNull);
			else if (!isLastIssue(issue, issueGroup))
				response.setState(ErrorCode.IssueHasFinished);
			else if (!isRelationalUser(personInCharge, project))
				response.setState(ErrorCode.UserIsNotInProject);
			else {
				issue.setFinishTime(new Date());
				issue = issueRepository.save(issue);

				newIssue.setDescription(request.getDescription());
				newIssue.setFinishTime(null);
				newIssue.setIssueGroup(issueGroup);
				newIssue.setPersonInChargeId(personInCharge);
				newIssue.setPriority(request.getPriority());
				newIssue.setReporterId(user);
				newIssue.setReportTime(new Date());
				newIssue.setServerity(request.getServerity());
				newIssue.setState(request.getState());
				newIssue.setTitle(request.getTitle());
				newIssue = issueRepository.save(newIssue);

				issueGroup = addIssue2IssueGroup(newIssue, issueGroup);
				project = addIssueGroup2Project(issueGroup, project);

				SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd a hh:mm");
				String message = new TerminalToHtml().append(personInCharge.getName()).append("你好：").enter()
						.append("專案").append(project.getName()).setBold(true).setColor(0, 0, 255).append("有一個新議題被指派給你")
						.enter().append("以下為議題內容").enter().enter().append("標題：").append(newIssue.getTitle()).enter()
						.append("描述：").append(newIssue.getDescription()).enter().append("提出者：").append(user.getName())
						.enter().append("指派時間：").append(sdFormat.format(newIssue.getReportTime())).enter()
						.append("請記得登入系統確認並回覆議題").enter().append("祝你有美好的一天").toHtml();
				emailService.generateAndSendEmail(personInCharge.getEmailAddress(), project.getName() + "有新的議題被指派",
						message);
				response.setState(ErrorCode.Correct);
				response.setIssueId(newIssue.getId());
			}
		} else if (isReporter(user, issue) || isProjectManager(user, issue)) {
			User projectManager = userRepository.findOne(request.getPersonInChargeId());
			if (isNull(projectManager))
				response.setState(ErrorCode.PersonInChargeNull);

			issue.setDescription(request.getDescription());
			issue.setPriority(request.getPriority());
			issue.setServerity(request.getServerity());
			issue.setState(request.getState());
			issue.setTitle(request.getTitle());
			issue.setPersonInChargeId(projectManager);
			issue = issueRepository.save(issue);

			// Project project = issue.getIssueGroup().getProject();
			// SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy-MM-dd a
			// hh:mm");
			// String message = new
			// TerminalToHtml().append(projectManager.getName()).append("你好：").enter().append("專案")
			// .append(project.getName()).setBold(true).setColor(0, 0,
			// 255).append("有一個新的議題被回報").enter()
			// .append("以下為議題內容").enter().enter().append("標題：").append(issue.getTitle()).enter().append("描述：")
			// .append(issue.getDescription()).enter().append("回報人：").append(user.getName()).enter()
			// .append("指派時間：").append(sdFormat.format(issue.getReportTime())).enter().enter()
			// .append("請記得登入系統完成議題指派").setBold(true).enter().append("祝你有美好的一天").toHtml();
			// emailService.generateAndSendEmail(projectManager.getEmailAddress(),
			// project.getName() + "有新的議題被回報",
			// message);
			response.setState(ErrorCode.Correct);
			response.setIssueId(issue.getId());
		} else {
			response.setState(ErrorCode.NotMember);
		}

		return response;
	}

	private boolean isLastIssue(Issue issue, IssueGroup issueGroup) {
		return issueGroup.getLastIssueId() == issue.getId();
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

	private boolean isSystemManager(User user) {
		return user.getRole().equals("SystemManager");
	}

	private boolean isRelationalUser(User user, Project project) {
		Set<MemberGroup> list = project.getMemberGroup();
		for (MemberGroup member : list) {
			if (user.getId() == member.getUser().getId()) {
				return true;
			}
		}
		return false;
	}
}
