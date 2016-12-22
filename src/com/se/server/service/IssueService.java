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
	MemberGroupRepository memberGroupRepository;
	
	@RequestMapping(value = "/issues/{userId}/{projectId}", method = RequestMethod.POST)
	public void createIssue(@PathVariable int userId,@PathVariable int projectId,@RequestBody int a){
		
	}
	
	@RequestMapping(value = "/issues/{userId}/{issueId}", method = RequestMethod.GET)
	public void getIssueInfo(@PathVariable int userId,@PathVariable int issueId){
		
	}
	
	@RequestMapping(value = "/issues/List/{userId}", method = RequestMethod.GET)
	public void getIssueListByUserId(@PathVariable int userId){
		
	}
	
	@RequestMapping(value = "/issues/List/{userId}/{projectId}", method = RequestMethod.GET)
	public void getIssueListByProjectId(@PathVariable int userId,@PathVariable int projectId){
		
	}
	
	@RequestMapping(value = "/issues/{userId}", method = RequestMethod.GET)
	public void getAllIssueList(@PathVariable int userId){
		
	}
	
	@RequestMapping(value = "/issues/{userId}/{issueId}", method = RequestMethod.PUT)
	public void updateIssue(@PathVariable int userId,@PathVariable int issueId,@RequestBody int a){
		
	}
	
	
}
