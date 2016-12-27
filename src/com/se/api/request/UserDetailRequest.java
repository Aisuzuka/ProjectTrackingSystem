package com.se.api.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDetailRequest {
	private String name;
	private String password;
	private String emailAddress;
	private String userRole;
	
}
