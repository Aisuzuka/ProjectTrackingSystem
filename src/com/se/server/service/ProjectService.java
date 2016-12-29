package com.se.server.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.se.api.data.ErrorCode;
import com.se.api.data.ProjectData;
import com.se.api.request.ProjectRequest;
import com.se.api.response.ProjectItemResponse;
import com.se.api.response.ProjectListResponse;
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
public class ProjectService {
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

	@RequestMapping(value = "/projects/{userId}", method = RequestMethod.POST)
	public ProjectItemResponse createProject(@PathVariable int userId, @RequestBody ProjectRequest request) {

		//Step1: check user id is valid
		User user = userRepository.findOne(userId);
		if (user == null){
			ProjectItemResponse response = new ProjectItemResponse();
			response.setState(ErrorCode.UserNull);
			return response;
		}
		
		//Step2: check user is general user
		if(user.getRole().equals("SystemManager")){
			ProjectItemResponse response = new ProjectItemResponse();
			response.setState(ErrorCode.NotGeneralUser);
			return response;
		}
		
		//Step3: create project
		Project project = new Project();//set project
		project.setName(request.getProjectName());
		project.setDescription(request.getDescription());
		project.setTimeStamp(new Date());
		project.setManager(user);
		user.getResponsibleProject().add(project);//set user relation
		MemberGroup memberGroup = new MemberGroup();//set member
		memberGroup.setUser(user);
		memberGroup.setRole("ProjectManager");
		memberGroup.setProject(project);
		memberGroup.setJoined(true);
		project.getMemberGroup().add(memberGroup);//set member relation
		user.getJoinMemberGroups().add(memberGroup);//set member relation
		project = projectRepository.save(project);

		//Step4: return response
		ProjectItemResponse response = new ProjectItemResponse();
		response.setState(0);
		ProjectData projectData = new ProjectData();
		projectData.setProjectId(project.getId());
		projectData.setDescription(project.getDescription());
		projectData.setProjectName(project.getName());
		projectData.setManager(user.getName());
		projectData.setTimeStamp(project.getTimeStamp());
		response.setProject(projectData);
		return response;	


	}

	@RequestMapping(value = "/projects/{userId}/{projectId}", method = RequestMethod.GET)
	public ProjectItemResponse getProjectInfo(@PathVariable int userId, @PathVariable int projectId) {
		
		//Step1: check project id is valid
		Project project = projectRepository.findOne(projectId);
		if (project == null) {
			ProjectItemResponse response = new ProjectItemResponse();
			response.setState(ErrorCode.ProjectNull);
			return response;
		}
		
		//Step2: check user is in project
		Set<MemberGroup> memberGroupSet = project.getMemberGroup();
		boolean findUser = false;
		for (MemberGroup memberGroup : memberGroupSet) {
			if (memberGroup.getUser().getId() == userId) {
				findUser = true;
				break;
			}
		}
		if (project.getManager().getId() == userId)findUser = true;
		if (!findUser) {
			ProjectItemResponse response = new ProjectItemResponse();
			response.setState(ErrorCode.NotMember);
			return response;
		}
		
		//Step3: return response
		ProjectItemResponse response = new ProjectItemResponse();
		response.setState(0);
		ProjectData projectData = new ProjectData();
		projectData.setProjectId(project.getId());
		projectData.setDescription(project.getDescription());
		projectData.setProjectName(project.getName());
		projectData.setManager(project.getManager().getName());
		projectData.setTimeStamp(project.getTimeStamp());
		response.setProject(projectData);
		return response;
			
		

	}

	@RequestMapping(value = "/projects/list/{userId}", method = RequestMethod.GET)
	public ProjectListResponse getProjectListByUserId(@PathVariable int userId) {
		//Step1: check user id is valid
		User user = userRepository.findOne(userId);
		if (user == null) {
			ProjectListResponse response = new ProjectListResponse();
			response.setState(ErrorCode.UserNull);
			return response;
		}
		
		//Step2: check user is general user
		if(!user.getRole().equals("GeneralUser")){
			ProjectListResponse response = new ProjectListResponse();
			response.setState(ErrorCode.NotGeneralUser);
			return response;
		}
		
		//Step3: return response
		Set<MemberGroup> memberGroupSet = user.getJoinMemberGroups();
		if (memberGroupSet.isEmpty()) {
			ProjectListResponse response = new ProjectListResponse();
			response.setState(0);
			response.setList(new ArrayList<ProjectData>());
			return response;
		}
		ProjectListResponse response = new ProjectListResponse();
		response.setState(0);
		response.setList(new ArrayList<ProjectData>());
		for(MemberGroup memberGroup:memberGroupSet){
			if(memberGroup.isJoined()){
				ProjectData projectData=new ProjectData();
				projectData.setProjectId(memberGroup.getProject().getId());
				projectData.setDescription(memberGroup.getProject().getDescription());
				projectData.setProjectName(memberGroup.getProject().getName());
				projectData.setManager(memberGroup.getProject().getManager().getName());
				projectData.setTimeStamp(memberGroup.getProject().getTimeStamp());
				response.getList().add(projectData);
			}

		}
		return response;

	}

	@RequestMapping(value = "/projects/{userId}", method = RequestMethod.GET)
	public ProjectListResponse getInvitedProjectListByUserId(@PathVariable int userId) {
		
		//Step1: check user id is valid
		User user = userRepository.findOne(userId);
		if (user == null) {
			ProjectListResponse response = new ProjectListResponse();
			response.setState(ErrorCode.UserNull);
			return response;
		}
		
		//Step2: check user is general user
		if(!user.getRole().equals("GeneralUser")){
			ProjectListResponse response = new ProjectListResponse();
			response.setState(ErrorCode.NotGeneralUser);
			return response;
		}
		
		
		//Step3: return response
		Set<MemberGroup> memberGroupSet = user.getJoinMemberGroups();
		if (memberGroupSet.isEmpty()) {
			ProjectListResponse response = new ProjectListResponse();
			response.setState(-2);
			response.setList(new ArrayList<ProjectData>());
			return response;
		}
		ProjectListResponse response = new ProjectListResponse();
		response.setState(0);
		response.setList(new ArrayList<ProjectData>());
		for (MemberGroup memberGroup : memberGroupSet) {
			if (!memberGroup.isJoined()) {
				ProjectData projectData = new ProjectData();
				projectData.setProjectId(memberGroup.getProject().getId());
				projectData.setDescription(memberGroup.getProject().getDescription());
				projectData.setProjectName(memberGroup.getProject().getName());
				projectData.setManager(memberGroup.getProject().getManager().getName());
				projectData.setTimeStamp(memberGroup.getProject().getTimeStamp());
				response.getList().add(projectData);
			}

		}
		return response;
	}

	@RequestMapping(value = "/all-projects/{userId}", method = RequestMethod.GET)
	public ProjectListResponse getAllProjectList(@PathVariable int userId) {
		
		//Step1: check user id is valid
		User user = userRepository.findOne(userId);
		if (user == null) {
			ProjectListResponse response = new ProjectListResponse();
			response.setState(ErrorCode.UserNull);
			return response;
		}

		//Step2: check user is system manager
		if(!user.getRole().equals("SystemManager")){
			ProjectListResponse response =new ProjectListResponse();
			response.setState(ErrorCode.NotSystemManager);
			return response;
		}
		
		//Step3: return response
		List<Project> projectList = IteratorUtils.toList(projectRepository.findAll().iterator());
		ProjectListResponse response = new ProjectListResponse();
		response.setState(0);
		response.setList(new ArrayList<ProjectData>());
		for (Project project : projectList) {
			ProjectData projectData = new ProjectData();
			projectData.setProjectId(project.getId());
			projectData.setDescription(project.getDescription());
			projectData.setProjectName(project.getName());
			projectData.setManager(project.getManager().getName());
			projectData.setTimeStamp(project.getTimeStamp());
			response.getList().add(projectData);
		}
		return response;

	}
	
	@RequestMapping(value = "/projects/put/{userId}/{projectId}", method = RequestMethod.POST)
	public int updateProjectInfo(@PathVariable int userId,@PathVariable int projectId,@RequestBody ProjectRequest request){
		
		//Step1: check project id is valid
		Project project = projectRepository.findOne(projectId);
		if (project == null) {
			return ErrorCode.ProjectNull;
		}
		
		//Step2: check user is project manager
		if(project.getManager().getId()!=userId){
			return ErrorCode.NotProjectManager;
		}

		//Step3: save update
		project.setDescription(request.getDescription());
		project.setName(request.getProjectName());
		projectRepository.save(project);
		return 0;
	}
	
	@RequestMapping(value = "/projects/delete/{userId}/{projectId}", method = RequestMethod.GET)
	public int deleteProject(@PathVariable int userId,@PathVariable int projectId){
		Project project = projectRepository.findOne(projectId);
		//Step1:check projectId is valid
		if(project == null){
			return ErrorCode.ProjectNull;
		}
		
		//Step2:check user is projectManager
		if(project.getManager().getId() != userId){
			return ErrorCode.NotProjectManager;
		}
		
		//Step3: delete relationship
		Set<MemberGroup> memberGroupSet= project.getMemberGroup();
		for(MemberGroup memberGroup:memberGroupSet){
			memberGroup.getUser().getJoinMemberGroups().remove(memberGroup);
			memberGroup.setUser(null);
		}
		User manager =project.getManager();
		manager.getResponsibleProject().remove(project);
		project.setManager(null);
		Set<IssueGroup> issueGroupSet = project.getIssueGroup();
		for (IssueGroup issueGroup : issueGroupSet) {
			Set<Issue> issueSet = issueGroup.getIssues();
			for(Issue issue : issueSet){
				issue.getPersonInChargeId().getHandleIssue().remove(issue);
				issue.setPersonInChargeId(null);
				issue.getReporterId().getResponsibleIssue().remove(issue);
				issue.setReporterId(null);
			}
		}
		projectRepository.delete(projectId);

		return 0;
	}
}
