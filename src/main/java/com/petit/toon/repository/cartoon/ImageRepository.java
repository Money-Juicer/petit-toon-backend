package com.petit.toon.repository.cartoon;

import com.petit.toon.entity.cartoon.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {

}
