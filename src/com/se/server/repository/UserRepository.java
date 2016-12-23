package com.se.server.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.se.server.entity.*;

public interface UserRepository extends CrudRepository<User, Integer>{
//	@Query(value = "Select * from USER where NAME = ?1", nativeQuery=true)
	public User findByName(String name); 
			
}
