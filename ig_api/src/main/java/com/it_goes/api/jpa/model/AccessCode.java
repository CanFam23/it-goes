package com.it_goes.api.jpa.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.security.SecureRandom;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@ToString
@Entity
@Table(name="accessCode", indexes = {
        @Index(name = "idx_code", columnList = "code", unique = true),
        @Index(name = "idx_email", columnList = "email", unique = true)
})
public class AccessCode {
    private static final int CODE_VALID_DAYS = 7;

    private static final int CODE_LENGTH = 8;
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generateCode(){
        final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

        final StringBuilder code = new StringBuilder();

        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = RANDOM.nextInt(characters.length());
            code.append(characters.charAt(index));
        }

        return code.toString();
    }

    public AccessCode(String email) {
        this.email = email;
        this.code = generateCode();
        this.expiresAt = LocalDateTime.now().plusDays(CODE_VALID_DAYS);
    }

    public void renew(String replacement) {
        if (used) {
            throw new IllegalStateException("Access code is already used");
        }
        code = replacement;
        expiresAt = LocalDateTime.now().plusDays(CODE_VALID_DAYS);
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length=CODE_LENGTH)
    private String code;

    @Column(unique = true)
    private String email;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column()
    private LocalDateTime expiresAt;

    @Column()
    private LocalDateTime usedAt;

    @Column()
    private boolean used;

    public void setUsed(){
        if (this.used) {
            throw new IllegalStateException("Access code is already used");
        }
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }

    public boolean hasBeenUsed(){
        return this.used;
    }
}
