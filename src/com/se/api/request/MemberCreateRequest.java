package com.se.api.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberCreateRequest {
	private int userId;
	private String role;
}
