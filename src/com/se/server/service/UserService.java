package com.se.server.service;

import java.util.ArrayList;
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
import com.se.tool.TerminalToHtml;


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
	@Autowired
	EmailService emailService;
	@Autowired
	ProjectService projectService;
	
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
	
	@RequestMapping(value = "/users/name/{userName}", method = RequestMethod.GET)
	public UserDetailResponse getUserInfoByName(@PathVariable String userName) {
		
		//Step1: check user id is valid
		User user = userRepository.findByName(userName);
		if (user == null) {
			UserDetailResponse response = new UserDetailResponse();
			response.setState(ErrorCode.UserNameNotExist);//-1
			return response;
		}
		
		//Step2: return response
		UserDetailResponse response = new UserDetailResponse();
		response.setState(0);
		response.setUserId(user.getId());
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
		
		//Step4: check user not in member
		if(request.getUserRole().equals("SystemManager") && user.getJoinMemberGroups()!=null && !user.getJoinMemberGroups().isEmpty()){
			return ErrorCode.UserHaveProjectOrInvite;
		}
		
		//Step5: save user
		if(request.getPassword()!= null && !request.getPassword().equals(""))
		user.setPassword(request.getPassword());
		if(request.getUserRole() != null && !request.getUserRole().equals(""))
		user.setRole(request.getUserRole());
		userRepository.save(user);

		return 0;

	}

	@RequestMapping(value = "/users/delete/{userId}/{delUserId}", method = RequestMethod.GET)
	public int deleteUserInfo(@PathVariable int userId, @PathVariable int delUserId) {
		
		//Step1: check user id is valid
		User user = userRepository.findOne(userId);
		if (user == null) {
			return ErrorCode.UserNull;
		}

		//Step2: check user is system manager
		if (!user.getRole().equals("SystemManager")) {
			return ErrorCode.NotSystemManager;
		}

		//Step3: check delete user id is valid
		user = userRepository.findOne(delUserId);
		if (user == null) {
			return ErrorCode.UserNull;
		}
		
		//Step4: delete project relationship
		Set<Project> projectSet = user.getResponsibleProject();
		List<Integer> projectList = new ArrayList<Integer>();
		for(Project project: projectSet){
			projectList.add(project.getId());
		}
		for(int projectId: projectList){
			projectService.deleteProject(user.getId(), projectId);
		}
		
		//Step5: delete handle issue relationship 
		Set<Issue> issueList = user.getHandleIssue();
		for(Issue issue :issueList){
			Project project= issue.getIssueGroup().getProject();
			User manager = project.getManager();
			issue.setPersonInChargeId(manager);
			manager.getHandleIssue().add(issue);
			userRepository.save(manager);
			String message = new TerminalToHtml().append("專案經理").append(manager.getName()).setColor(0, 0, 255).append("您好").enter().enter()
					.append("由於議題負責人").append(user.getName()).setColor(0, 0, 255).append("被系統管理員刪除").enter()
					.append("專案").append(project.getName()).setColor(0, 0, 255).append("中的").append(issue.getTitle()).setColor(0, 0, 255).append("議題將交給您處理").toHtml();
			emailService.generateAndSendEmail(manager.getEmailAddress(),"議題"+issue.getTitle()+"轉交給您處理",message);
		}
		user.setHandleIssue(null);
		
		//Step6: delete responsible issue relationship 
		issueList = user.getResponsibleIssue();
		for(Issue issue :issueList){
			Project project= issue.getIssueGroup().getProject();
			User manager = project.getManager();
			issue.setReporterId(manager);
			manager.getResponsibleIssue().add(issue);
			userRepository.save(manager);
			String message = new TerminalToHtml().append("專案經理").append(manager.getName()).setColor(0, 0, 255).append("您好").enter().enter()
					.append("由於議題提出人").append(user.getName()).setColor(0, 0, 255).append("被系統管理員刪除").enter()
					.append("專案").append(project.getName()).setColor(0, 0, 255).append("中的").append(issue.getTitle()).setColor(0, 0, 255).append("議題將由您負責").toHtml();
			emailService.generateAndSendEmail(manager.getEmailAddress(),"議題"+issue.getTitle()+"轉交給您負責",message);
		}
		user.setResponsibleIssue(null);
		
		
		
		//Step7: delete member relationship
		Set<MemberGroup> memberGroupSet =user.getJoinMemberGroups();
		for(MemberGroup memberGroup :memberGroupSet){
			memberGroup.getProject().getMemberGroup().remove(memberGroup);
			memberGroup.setProject(null);
		}

		//Step8: delete user
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
