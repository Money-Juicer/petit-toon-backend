package com.petit.toon.repository.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CustomCartoonRepository {

    List<Cartoon> findAllWithExactOrder(List<Long> ids);

    List<Cartoon> findAllWithUserWithExactOrder(List<Long> ids);

    List<Cartoon> findAllWithFollower(Long userId, Pageable pageable);
}
