package com.it_goes.api.jpa.model;

import com.it_goes.api.util.enums.Social;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "social_link")
/**
 * Entity class to represent a social link, like a link to a youtube or instagram account.
 * Mapped to {@User} by the `socialLinks` list in the `User` class.
 */
public class SocialLink {

    public SocialLink(String url, Social platformEnum) {
        this.url = url;
        this.platformEnum = platformEnum;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Setter
    @Column(nullable = false)
    private String url;

    @Setter
    @Column(nullable = false)
    private Social platformEnum;
}
