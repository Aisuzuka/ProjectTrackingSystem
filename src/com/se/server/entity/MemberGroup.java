package com.se.server.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "[MEMBERGROUP]")
@Getter
@Setter
public class MemberGroup {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private int id;
	
	//ok
	@JoinColumn(name = "USERSID")
	@ManyToOne(cascade=CascadeType.ALL,  optional=false)
	private User user;
	
	//ok
	@JoinColumn(name = "PROJECTID")
	@ManyToOne(cascade=CascadeType.ALL, optional=false)
	private Project project;
	
	@Column(name = "ROLE")
	private String role;
	
	@Column(name = "ISJOINED")
	private boolean isJoined;
	

}
