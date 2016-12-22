package com.se.api.response;

import java.util.List;

import com.se.api.data.ProjectData;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectListResponse {
	private int state;
	private List<ProjectData> list;
}
