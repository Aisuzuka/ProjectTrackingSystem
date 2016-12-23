package com.se.server.repository;

import org.springframework.data.repository.CrudRepository;

import com.se.server.entity.*;

public interface MemberGroupRepository extends CrudRepository<MemberGroup, Integer>{
	
	public MemberGroup findByProjectId(int projectId);
	
	public MemberGroup findByUserId(int userId);
}
