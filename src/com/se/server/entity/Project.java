package com.se.server.entity;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "[PROJECT]")
@Getter
@Setter
public class Project {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private int id;
	
	@Column(name = "NAME")
	private String name;
	
	@Column(name = "DESCRIPTION")
	private String description;
	
	//ok
	@JoinColumn(name = "_MANAGER")
	@ManyToOne(cascade=CascadeType.ALL, optional=true)
	private User manager;
	
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "TIMESTAMP")
	private Date timeStamp;
	
	//ok
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="project", orphanRemoval=true)
	private Set<MemberGroup> memberGroup = new HashSet<MemberGroup>();
	
	//ok
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="project", orphanRemoval=true)
	private Set<IssueGroup> issueGroup = new HashSet<IssueGroup>();
}
