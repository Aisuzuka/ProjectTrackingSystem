package com.se.api.response;

import java.util.List;

import com.se.api.data.Issue;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueListResponse {
	private int state;
	private List<Issue> list;
}
