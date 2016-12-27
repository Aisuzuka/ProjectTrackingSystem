package com.se.api.data;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IssueData {
	private int issueId;
	private int issueGroupId;
	private String title;
	private String description;
	private String state;
	private String serverity;
	private String priority;
	private int reporterId;
	private Date reportTime;
	private int personInChargeId;
	private Date finishTime;
}
