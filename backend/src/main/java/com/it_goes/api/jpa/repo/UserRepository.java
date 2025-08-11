package com.it_goes.api.jpa.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.it_goes.api.jpa.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
}
