package com.se.api.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDetailResponse {
	private int state;
	private int userId;
	private String emailAddress;
	private String userRole;
}
