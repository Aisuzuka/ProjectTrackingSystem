package com.se.server.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "[USER]")
@Getter
@Setter
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private int id;
	
	@Column(name = "NAME")
	private String name;
	
	@Column(name = "PASSWORD")
	private String password;
	
	@Column(name = "EMAILADDRESS")
	private String emailAddress;
	
	@Column(name = "ROLE")
	private String role;
	
	//ok
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY,mappedBy="user", orphanRemoval=true)
	private Set<MemberGroup> joinMemberGroups=new HashSet<MemberGroup>();
	
	//ok
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="manager", orphanRemoval=true)
	private Set<Project> responsibleProject=new HashSet<Project>();
	
	//ok
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="reporterId", orphanRemoval=true)
	private Set<Issue> responsibleIssue=new HashSet<Issue>();
	
	//ok
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="personInChargeId", orphanRemoval=true)
	private Set<Issue> handleIssue= new HashSet<Issue>();
 	
	
}
