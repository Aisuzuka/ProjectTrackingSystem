package com.se.api.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDetailRequest {
	private String name;
	private String passwrod;
	private String emailAddress;
	private String userRoel;
}
