package com.se.api.response;

import com.se.api.data.UserData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserItemResponse {
	private int state;
	private UserData user;
}
