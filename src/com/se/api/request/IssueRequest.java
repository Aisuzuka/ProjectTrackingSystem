package com.se.api.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueRequest {
	private String title;
	private String description;
	private String state;
	private String serverity;
	private String priority;
	private int personInChargeId;
}
