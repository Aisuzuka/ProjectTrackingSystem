package com.se.api.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Member {
	private int userId;
	private String role;
	private String isJoined;
}
