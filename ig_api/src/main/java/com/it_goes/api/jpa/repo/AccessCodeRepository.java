package com.it_goes.api.jpa.repo;

import com.it_goes.api.jpa.model.AccessCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface AccessCodeRepository extends JpaRepository<AccessCode, Long> {
    Optional<AccessCode> findByCode(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AccessCode a where a.code = :code")
    Optional<AccessCode> findByCodeForUpdate(@Param("code") String code);

    boolean existsByCode(String code);

    Optional<AccessCode> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AccessCode a where a.email = :email")
    Optional<AccessCode> findByEmailForUpdate(@Param("email") String email);
}
