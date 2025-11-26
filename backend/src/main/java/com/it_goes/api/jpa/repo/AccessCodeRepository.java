package com.it_goes.api.jpa.repo;

import com.it_goes.api.jpa.model.AccessCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccessCodeRepository extends JpaRepository<AccessCode, Long> {
    Optional<AccessCode> findByCode(String code);

    Optional<AccessCode> findByEmail(String email);
}
