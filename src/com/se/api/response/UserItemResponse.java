package com.se.api.response;

import com.se.api.data.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserItemResponse {
	private int state;
	private User user;
}
