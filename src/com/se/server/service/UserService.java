package com.se.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.se.server.entity.User;
import com.se.server.repository.*;

@RestController
@RequestMapping(value = "/api")
@Transactional("jpaTransactionManager")
public class UserService {
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
	
	@RequestMapping(value = "/register", method = RequestMethod.POST)
	public int register(int id){
		User user = new User();
		user.setEmailAddress("test");
		user.setName("user");
		user.setPassword("password");
		user.setRole("role");
		userRepository.save(user);
		return 0;
	}
	
	@RequestMapping(value = "/users", method = RequestMethod.POST)
	public void createUser(@RequestBody int a){
		
	}
	
	@RequestMapping(value = "/users/{userId}", method = RequestMethod.GET)
	public void getUserInfo(@PathVariable int userId){
		
	}
	
	@RequestMapping(value = "/users/List/{userId}", method = RequestMethod.GET)
	public void getUserList(@PathVariable int userId){
		
	}
	
	@RequestMapping(value = "/users/{userId}", method = RequestMethod.PUT)
	public void updateUserInfo(@PathVariable int userId,@RequestBody int a){
		
	}
	
	@RequestMapping(value = "/users/{userId}/{delUserId}", method = RequestMethod.DELETE)
	public void deleteUserInfo(@PathVariable int userId,@PathVariable int delUserId){
		
	}
	
	@RequestMapping(value = "/session", method = RequestMethod.POST)
	public void login(@RequestBody int a){
		
	}
	

}
