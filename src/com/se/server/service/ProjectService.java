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

import com.se.api.data.ProjectData;
import com.se.api.request.ProjectRequest;
import com.se.api.response.ProjectItemResponse;
import com.se.api.response.ProjectListResponse;
import com.se.api.response.UserListResponse;
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
	public ProjectItemResponse createProject(@PathVariable int userId,@RequestBody ProjectRequest request ){
		
		User user =userRepository.findOne(userId);
		if(user != null && !user.getRole().equals("manager")){
			Project project  = new Project();
			project.setName(request.getProjectName());
			project.setDescription(request.getDescription());
			project.setTimeStamp(new Date());
			project.setManager(user);
			user.getResponsibleProject().add(project);
			userRepository.save(user);
			
			ProjectItemResponse  response = new ProjectItemResponse();
			response.setState(0);
			
			ProjectData projectData=new ProjectData();
			projectData.setDescription(project.getDescription());
			projectData.setProjectName(project.getName());	
			projectData.setManager(user.getName());
			projectData.setTimeStamp(project.getTimeStamp());
			return response;
		}else{
			
			ProjectItemResponse  response = new ProjectItemResponse();
			response.setState(-1);
			return response;
		}
		
		
	}
	
	@RequestMapping(value = "/projects/{userId}/{projectId}", method = RequestMethod.GET)
	public ProjectItemResponse getProjectInfo(@PathVariable int userId,@PathVariable int projectId){
		Project project = projectRepository.findOne(projectId);
		if(project == null){
			ProjectItemResponse  response = new ProjectItemResponse();
			response.setState(-1);
			return response;
		}
		Set<MemberGroup> memberGroupSet= project.getMemberGroup();
		boolean findUser =false;
		for(MemberGroup memberGroup:memberGroupSet){
			if(memberGroup.getUser().getId()==userId){
				findUser =true;
				break;
			}
		}
		if(!findUser){
			ProjectItemResponse  response = new ProjectItemResponse();
			response.setState(-1);
			return response;
		}else{
			ProjectItemResponse  response = new ProjectItemResponse();
			response.setState(0);
			
			ProjectData projectData=new ProjectData();
			projectData.setDescription(project.getDescription());
			projectData.setProjectName(project.getName());	
			projectData.setManager(project.getManager().getName());
			projectData.setTimeStamp(project.getTimeStamp());
			return response;
		}
		
		
		
	}
	
	@RequestMapping(value = "/projects/list/{userId}", method = RequestMethod.GET)
	public ProjectListResponse getProjectListByUserId(@PathVariable int userId){
		User user =userRepository.findOne(userId);
		if(user== null){
			ProjectListResponse response =new ProjectListResponse();
			response.setState(-1);
			response.setList(new ArrayList<ProjectData>());
			return response;
		}
		
		Set<MemberGroup> memberGroupSet =user.getJoinMemberGroups();
		if(memberGroupSet.isEmpty()){
			ProjectListResponse response =new ProjectListResponse();
			response.setState(0);
			response.setList(new ArrayList<ProjectData>());
			return response;
		}
		
		ProjectListResponse response =new ProjectListResponse();
		response.setState(0);
		response.setList(new ArrayList<ProjectData>());
		for(MemberGroup memberGroup:memberGroupSet){
			if(memberGroup.isJoined()){
				ProjectData projectData=new ProjectData();
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
	public ProjectListResponse getInvitedProjectListByUserId(@PathVariable int userId){
		User user =userRepository.findOne(userId);
		if(user== null){
			ProjectListResponse response =new ProjectListResponse();
			response.setState(-1);
			response.setList(new ArrayList<ProjectData>());
			return response;
		}
		
		Set<MemberGroup> memberGroupSet =user.getJoinMemberGroups();
		if(memberGroupSet.isEmpty()){
			ProjectListResponse response =new ProjectListResponse();
			response.setState(0);
			response.setList(new ArrayList<ProjectData>());
			return response;
		}
		
		ProjectListResponse response =new ProjectListResponse();
		response.setState(0);
		response.setList(new ArrayList<ProjectData>());
		for(MemberGroup memberGroup:memberGroupSet){
			if(!memberGroup.isJoined()){
				ProjectData projectData=new ProjectData();
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
	public ProjectListResponse getAllProjectList(@PathVariable int userId){
		User user =userRepository.findOne(userId);
		if(user== null){
			ProjectListResponse response =new ProjectListResponse();
			response.setState(-1);
			response.setList(new ArrayList<ProjectData>());
			return response;
		}
		if(!user.getRole().equals("manager")){
			ProjectListResponse response =new ProjectListResponse();
			response.setState(-1);
			response.setList(new ArrayList<ProjectData>());
			return response;
		}
		List<Project> projectList = IteratorUtils.toList(projectRepository.findAll().iterator());
		
		ProjectListResponse response =new ProjectListResponse();
		response.setState(0);
		response.setList(new ArrayList<ProjectData>());
		for(Project project : projectList){
			ProjectData projectData=new ProjectData();
			projectData.setDescription(project.getDescription());
			projectData.setProjectName(project.getName());	
			projectData.setManager(project.getManager().getName());
			projectData.setTimeStamp(project.getTimeStamp());
			response.getList().add(projectData);
		}
		return response;
		
	}
	
	@RequestMapping(value = "/projects/{userId}/{projectId}", method = RequestMethod.PUT)
	public int updateProjectInfo(@PathVariable int userId,@PathVariable int projectId,@RequestBody ProjectRequest request){
		Project project = projectRepository.findOne(projectId);
		if(project == null){
			return -1;
		}
		Set<MemberGroup> memberGroupSet= project.getMemberGroup();
		boolean findUser =false;
		for(MemberGroup memberGroup:memberGroupSet){
			if(memberGroup.getUser().getId()==userId){
				findUser =true;
				break;
			}
		}
		if(!findUser){
			return -1;
		}
		
		project.setDescription(request.getDescription());
		project.setName(request.getProjectName());
		
		projectRepository.save(project);
		return 0;
	}
	
	@RequestMapping(value = "/projects/{userId}/{projectId}", method = RequestMethod.DELETE)
	public int deleteProject(@PathVariable int userId,@PathVariable int projectId){
		Project project = projectRepository.findOne(projectId);
		if(project == null){
			return -1;
		}
		Set<MemberGroup> memberGroupSet= project.getMemberGroup();
		boolean findUser =false;
		for(MemberGroup memberGroup:memberGroupSet){
			if(memberGroup.getUser().getId()==userId){
				findUser =true;
				break;
			}
		}
		if(!findUser){
			return -1;
		}
		
		memberGroupSet=project.getMemberGroup();
		for(MemberGroup memberGroup:memberGroupSet){
			memberGroup.getUser().getJoinMemberGroups().remove(memberGroup);
			memberGroup.setUser(null);
		}
		
		User manager =project.getManager();
		manager.getResponsibleProject().remove(manager);
		project.setManager(null);
		
		
		Set<IssueGroup> issueGroupSet = project.getIssueGroup();
		for(IssueGroup issueGroup:issueGroupSet){
			Set<Issue> issueSet = issueGroup.getIssues();
			for(Issue issue : issueSet){
				issue.setIssueGroup(null);
				issue.getPersonInChagedId().getHandleIssue().remove(issue);
				issue.getReporterId().getResponsibleIssue().remove(issue);
			}
			issueGroup.setIssues(null);
		}
		project.setIssueGroup(null);
		
		projectRepository.delete(project.getId());
		
		
		
		return 0;
	}
}
