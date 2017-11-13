package com.ef.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * Blocked IP
 * 
 * @author victor
 *
 */
@Entity
@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Table(name = "blocked_ip")
public class BlockedIP {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NonNull
	private String ip;

}
