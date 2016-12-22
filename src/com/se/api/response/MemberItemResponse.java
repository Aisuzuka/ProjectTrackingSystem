package com.se.api.response;

import com.se.api.data.Member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberItemResponse {
	private int state;
	private Member member;
}
