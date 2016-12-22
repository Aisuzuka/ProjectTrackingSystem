package com.se.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
	public void createProject(@PathVariable int userId,@RequestBody int a){
		
	}
	
	@RequestMapping(value = "/projects/{userId}/{projectId}", method = RequestMethod.GET)
	public void getProjectInfo(@PathVariable int userId,@RequestBody int a){
		
	}
	
	@RequestMapping(value = "/projects/list/{userId}", method = RequestMethod.GET)
	public void getProjectListByUserId(@PathVariable int userId){
		
	}
	
	@RequestMapping(value = "/projects/{userId}", method = RequestMethod.GET)
	public void getInvitedProjectListByUserId(@PathVariable int userId){
		
	}
	
	@RequestMapping(value = "/all-projects/{userId}", method = RequestMethod.GET)
	public void getAllProjectList(@PathVariable int userId){
		
	}
	
	@RequestMapping(value = "/projects/{userId}/{projectId}", method = RequestMethod.PUT)
	public void updateProjectInfo(@PathVariable int userId,@PathVariable int projectId,@RequestBody int a){
		
	}
	
	@RequestMapping(value = "/projects/{userId}/{projectId}", method = RequestMethod.DELETE)
	public void deleteProject(@PathVariable int userId,@PathVariable int projectId){
		
	}
}
