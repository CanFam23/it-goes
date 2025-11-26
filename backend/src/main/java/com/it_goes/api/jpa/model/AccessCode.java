package com.it_goes.api.jpa.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@ToString
@Entity
@Table(name="accessCode", indexes = {
        @Index(name = "idx_access_code", columnList = "accessCode", unique = true),
        @Index(name = "idx_email", columnList = "email", unique = true)
})
public class AccessCode {
    private static final int CODE_VALID_DAYS = 7;

    private static final int CODE_LENGTH = 8;

    private static String generateCode(){
        final String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

        final StringBuilder code = new StringBuilder();

        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = (int) (Math.random() * characters.length());
            code.append(characters.charAt(index));
        }

        return code.toString();
    }

    public AccessCode(String email) {
        this.email = email;

        this.expiresAt = LocalDateTime.now().plusDays(CODE_VALID_DAYS);

        this.code = generateCode();
        this.used = false;
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
