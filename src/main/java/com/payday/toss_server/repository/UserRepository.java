package com.payday.toss_server.repository;

import com.payday.toss_server.Entity.Request;
import com.payday.toss_server.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>  {
    Optional<User> findByUserId(long userId);
}
