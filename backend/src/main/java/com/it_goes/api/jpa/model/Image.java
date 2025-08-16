package com.it_goes.api.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "image")
public class Image {

    public Image(String s3Key, String imageUrl, String imageName){
        this.s3Key = s3Key;
        this.imageUrl = imageUrl;
        this.imageName = imageName;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(nullable = false)
    private String s3Key;

    @Setter
    @Column(nullable = false)
    private String imageUrl;

    @Setter
    @Column(nullable = false)
    private String imageName;
}
