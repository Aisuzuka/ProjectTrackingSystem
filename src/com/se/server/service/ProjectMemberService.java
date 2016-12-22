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
	public void createMember(@PathVariable int userId,@PathVariable int projectId,@RequestBody int a){
		
	}
	
	@RequestMapping(value = "/members/list/{userId}/{projectId}", method = RequestMethod.GET)
	public void getMemberByProjectId(@PathVariable int userId,@PathVariable int projectId){
		
	}
	
	@RequestMapping(value = "/members/{userId}/{projectId}", method = RequestMethod.PUT)
	public void updateInfo(@PathVariable int userId,@PathVariable int projectId,@RequestBody int a){
		
	}
	
	@RequestMapping(value = "/members/{userId}/{projectId}/{memberId}", method = RequestMethod.PUT)
	public void updateUserPermissionByUserId(@PathVariable int userId,@PathVariable int projectId,@PathVariable int memberId,@RequestBody String member){
		
	}
	
	@RequestMapping(value = "/members/{userId}/{projectId}", method = RequestMethod.DELETE)
	public void deleteMember(@PathVariable int userId,@PathVariable int projectId){
		
	}
	
	@RequestMapping(value = "/members/{userId}/{projectId}", method = RequestMethod.PUT)
	public void replayProjectInvite(@PathVariable int userId,@PathVariable int projectId,@RequestBody boolean isAccepted  ){
		
	}
}
