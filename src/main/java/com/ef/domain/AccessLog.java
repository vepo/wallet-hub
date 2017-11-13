package com.ef.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Access Log Entry
 * 
 * @author victor
 *
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "log_access")
public class AccessLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NonNull
	private Date time;

	@NonNull
	private String ip;

	@NonNull
	private String request;

	@NonNull
	@Column(name = "response_code")
	private Integer responseCode;

	@ManyToOne
	@JoinColumn(name = "agent_id")
	private Agent agent;
}
