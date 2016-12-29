package com.se.server.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.collections.IteratorUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.se.api.data.ErrorCode;
import com.se.api.data.UserData;
import com.se.api.request.UserCreateRequest;
import com.se.api.request.UserDetailRequest;
import com.se.api.request.UserSessionRequest;
import com.se.api.response.UserDetailResponse;
import com.se.api.response.UserListResponse;
import com.se.api.response.UserSessionResponse;
import com.se.server.entity.Issue;
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

	@RequestMapping(value = "/users", method = RequestMethod.POST)
	public UserSessionResponse createUser(@RequestBody UserCreateRequest request) {
		
		//Step1: check user name is repeat
		User exitUser = userRepository.findByName(request.getName());
		if (exitUser != null) {
			UserSessionResponse response = new UserSessionResponse();
			response.setState(ErrorCode.UserNameRepeat);//-1
			return response;
		}

		//Step2: check email format is valid
		try {
		    InternetAddress emailAddr = new InternetAddress(request.getEmailAddress());
		    emailAddr.validate();
		}catch (AddressException ex) {
			UserSessionResponse response = new UserSessionResponse();
			response.setState(ErrorCode.EmailFormatNotValid); //-2
			return response;
		}
		
		//Step3: create user
		User user = new User();
		user.setName(request.getName());
		user.setPassword(request.getPassword());
		user.setEmailAddress(request.getEmailAddress());
		user.setRole("GeneralUser");
		user = userRepository.save(user);
		
		//Step4: return response
		UserSessionResponse response = new UserSessionResponse();
		response.setState(0);
		response.setUserId(user.getId());
		response.setUserRole(user.getRole());
		return response;

	}

	@RequestMapping(value = "/users/{userId}", method = RequestMethod.GET)
	public UserDetailResponse getUserInfo(@PathVariable int userId) {
		
		//Step1: check user id is valid
		User user = userRepository.findOne(userId);
		if (user == null) {
			UserDetailResponse response = new UserDetailResponse();
			response.setState(ErrorCode.UserNull);//-1
			return response;
		}
		
		//Step2: return response
		UserDetailResponse response = new UserDetailResponse();
		response.setState(0);
		response.setUserId(userId);
		response.setName(user.getName());
		response.setEmailAddress(user.getEmailAddress());
		response.setUserRole(user.getRole());
		return response;

	}

	@RequestMapping(value = "/users/list/{userId}", method = RequestMethod.GET)
	public UserListResponse getUserList(@PathVariable int userId) {
		
		//Step1: check user id is valid
		User user = userRepository.findOne(userId);
		if (user == null) {
			UserListResponse response = new UserListResponse();
			response.setState(ErrorCode.UserNull);
			response.setList(null);
			return response;
		}
		
		//Step2: check user id is system manager
		if (!user.getRole().equals("SystemManager")) {
			UserListResponse response = new UserListResponse();
			response.setState(ErrorCode.NotSystemManager);
			response.setList(null);
			return response;
		}
		
		//Step3: return response
		UserListResponse response = new UserListResponse();
		List<User> userList = IteratorUtils.toList(userRepository.findAll().iterator());
		List<UserData> userDataList = new ArrayList<UserData>();
		for (User u : userList) {
			UserData userData = new UserData();
			userData.setName(u.getName());
			userData.setUserId(u.getId());
			userData.setUserRole(u.getRole());
			userData.setEmailAddress(u.getEmailAddress());
			userDataList.add(userData);
		}
		response.setState(0);
		response.setList(userDataList);
		return response;
	}

	@RequestMapping(value = "/users/put/{userId}", method = RequestMethod.POST)
	public int updateUserInfo(@PathVariable int userId, @RequestBody UserDetailRequest request) {
		
		//Step1: check user id is valid
		User user = userRepository.findOne(userId);
		if (user == null) {
			return ErrorCode.UserNull;
		}
		
		//Step2: check user name is repeat
		if(request.getName()!= null && !request.getName().equals("")){
			if(userRepository.findByName(request.getName())!=null&&!request.getName().equals(user.getName()))return ErrorCode.UserNameRepeat;
			user.setName(request.getName());
		}
		
		//Step3: check email is valid
		if(request.getEmailAddress()!= null && !request.getEmailAddress().equals(""))
		try {
		    InternetAddress emailAddr = new InternetAddress(request.getEmailAddress());
		    emailAddr.validate();
		    user.setEmailAddress(request.getEmailAddress());
		}catch (AddressException ex) {

			return ErrorCode.EmailFormatNotValid;
		}
		
		//Step4: save user
		if(request.getPassword()!= null && !request.getPassword().equals(""))
		user.setPassword(request.getPassword());
		if(request.getUserRole() != null && !request.getUserRole().equals(""))
		user.setRole(request.getUserRole());
		userRepository.save(user);

		return 0;

	}

	@RequestMapping(value = "/users/delete/{userId}/{delUserId}", method = RequestMethod.GET)
	public int deleteUserInfo(@PathVariable int userId, @PathVariable int delUserId) {
		User user = userRepository.findOne(userId);
		if (user == null) {
			return -1;
		}

		if (!user.getRole().equals("SystemManager")) {
			return -2;
		}

		user = userRepository.findOne(delUserId);
		if (user == null) {
			return -3;
		}

		Set<Issue> issueList = user.getHandleIssue();

		if (!issueList.isEmpty()) {
			return -4;
		}

		issueList = user.getResponsibleIssue();

		if (!issueList.isEmpty()) {
			return -5;
		}

		Set<Project> projectList = user.getResponsibleProject();

		if (!projectList.isEmpty()) {
			return -6;
		}

		Set<MemberGroup> memberGroupList = user.getJoinMemberGroups();

		for (MemberGroup memberGroup : memberGroupList) {
			Iterator<MemberGroup> memberGroupIterator = memberGroup.getProject().getMemberGroup().iterator();
			while (memberGroupIterator.hasNext()) {
				if (memberGroupIterator.next().getId() == user.getId()) {
					memberGroupIterator.remove();
				}
				memberGroup.setProject(null);
			}
		}

		userRepository.delete(user.getId());

		return 0;

	}

	@RequestMapping(value = "/session", method = RequestMethod.POST)
	public UserSessionResponse login(@RequestBody UserSessionRequest request) {
		
		//Step1: check user name is valid
		User user = userRepository.findByName(request.getName());
		if (user == null) {
			UserSessionResponse userSessionResponse = new UserSessionResponse();
			userSessionResponse.setState(ErrorCode.UserNameNotExist);
			return userSessionResponse;
		}
		
		//Step2: check password is valid
		if (!user.getPassword().equals(request.getPassword())){
			UserSessionResponse userSessionResponse = new UserSessionResponse();
			userSessionResponse.setState(ErrorCode.PasswordNotVaild );
			return userSessionResponse;
		} 
		
		//Step3: return response
		UserSessionResponse userSessionResponse = new UserSessionResponse();
		userSessionResponse.setState(0);
		userSessionResponse.setUserId(user.getId());
		userSessionResponse.setUserRole(user.getRole());
		return userSessionResponse;

	}
	

}
