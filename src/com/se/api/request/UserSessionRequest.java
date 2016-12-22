package com.se.api.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSessionRequest {
	private String name;
	private String password;
}
