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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "[ISSUEGROUP]")
@Getter
@Setter
public class IssueGroup {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private int id;
	
	//ok
	@JoinColumn(name = "PROJECTID")
	@ManyToOne(cascade=CascadeType.ALL, optional=false)
	private Project project;
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="issueGroup", orphanRemoval=true)
	private Set<Issue> issues = new HashSet<Issue>();
	
//	//ok
//	@JoinColumn(name = "LASTISSUE")
//	@OneToOne(cascade=CascadeType.ALL, optional=false)
//	private Issue lastIssue;
	
	@Column(name = "LASTISSUEID")
	private int lastIssueId;

}
