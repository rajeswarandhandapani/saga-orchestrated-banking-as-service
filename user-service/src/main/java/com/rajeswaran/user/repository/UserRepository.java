package com.rajeswaran.user.repository;

import com.rajeswaran.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    // Optionally add custom query methods here
}
