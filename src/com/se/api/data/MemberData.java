package com.se.api.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberData {
	private int userId;
	private String role;
	private boolean isJoined;
	
	public boolean getJoined(){
		return isJoined;
	}
}
