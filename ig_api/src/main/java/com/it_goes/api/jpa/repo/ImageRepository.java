package com.it_goes.api.jpa.repo;

import com.it_goes.api.jpa.model.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
