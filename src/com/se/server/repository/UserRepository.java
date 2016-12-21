package com.se.server.repository;

import org.springframework.data.repository.CrudRepository;

import com.se.server.entity.*;

public interface UserRepository extends CrudRepository<User, Integer>{

}
