package com.ef.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ef.domain.BlockedIP;

@Repository
public interface BlockedIPRepository extends JpaRepository<BlockedIP, Long> {

}
