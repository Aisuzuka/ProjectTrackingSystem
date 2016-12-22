package com.se.api.response;

import java.util.List;

import com.se.api.data.Member;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberListResponse {
	private int state;
	private List<Member> member;
}
