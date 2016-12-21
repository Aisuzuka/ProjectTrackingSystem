package com.se.server.entity;

import java.util.Date;
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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "[ISSUE]")
@Getter
@Setter
public class Issue {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private int id;

	@Column(name = "TITLE")
	private String title;

	@Column(name = "DESCRIPTION")
	private String description;

	@Column(name = "STATE")
	private String state;

	@Column(name = "PRIORITY")
	private String priority;
	
	@Column(name = "SERVERITY")
	private String serverity;

	@JoinColumn(name = "ISSUEGROUPID")
	@ManyToOne(cascade = CascadeType.ALL, optional = false)
	private IssueGroup issueGroup;

	// ok
	@JoinColumn(name = "REPORTERID")
	@ManyToOne(cascade = CascadeType.ALL, optional = false)
	private User reporterId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "REPORTTIME")
	private Date reportTime;

	// ok
	@JoinColumn(name = "PERSONINGHARGEDID")
	@ManyToOne(cascade = CascadeType.ALL, optional = false)
	private User personInChagedId;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "FINISHTIME")
	private Date finishTime;

//	// ok
//	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "lastIssue", orphanRemoval = true, optional = true)
//	private IssueGroup issueGroup;

}
