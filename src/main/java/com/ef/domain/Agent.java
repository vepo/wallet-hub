package com.ef.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * HTTP Agent
 * 
 * @author victor
 *
 */
@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "agent", uniqueConstraints = { @UniqueConstraint(columnNames = { "description" }) })
public class Agent {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NonNull
	@Column(nullable = false, length = 65535, columnDefinition = "TEXT")
	private String description;
}