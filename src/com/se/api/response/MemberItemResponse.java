package com.se.api.response;

import com.se.api.data.MemberData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberItemResponse {
	private int state;
	private MemberData member;
}
