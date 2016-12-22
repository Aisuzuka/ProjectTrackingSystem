package com.se.api.response;

import com.se.api.data.Project;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectItemResponse {
	private int state;
	private Project project;
}
