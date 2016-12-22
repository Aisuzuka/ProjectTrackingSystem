package com.se.api.response;


import com.se.api.data.IssueData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueItemResponse {
	private int state;
	private IssueData issue;
}
