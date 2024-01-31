package com.payday.toss_server.repository;

import com.payday.toss_server.Entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RequestRepository extends JpaRepository<Request, String> {
}
