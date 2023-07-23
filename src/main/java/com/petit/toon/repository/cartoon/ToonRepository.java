package com.petit.toon.repository.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ToonRepository extends JpaRepository<Cartoon, Long> {

}
