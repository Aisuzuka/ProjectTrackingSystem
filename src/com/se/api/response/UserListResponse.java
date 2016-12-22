package com.se.api.response;

import java.util.List;

import com.se.api.data.UserData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserListResponse {
	private int state;
	private List<UserData> list;
}
