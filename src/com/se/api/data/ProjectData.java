package com.se.api.data;



import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectData {
	private String projectName;
	private String description;
	private String manager;
	private Date timeStamp;
}
