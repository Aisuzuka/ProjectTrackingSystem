package com.se.api.data;

import java.sql.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Project {
	private String projectName;
	private String description;
	private String manager;
	private Date timeStamp;
}
