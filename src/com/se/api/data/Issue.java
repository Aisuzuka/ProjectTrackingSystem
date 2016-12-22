package com.se.api.data;

import java.sql.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Issue {
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
