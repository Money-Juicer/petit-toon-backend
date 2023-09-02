package com.petit.toon.repository.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;

import java.util.List;

public interface CustomCartoonRepository {

    List<Cartoon> findAllWithUserWithExactOrder(List<Long> ids);

    List<Cartoon> findAllWithFollower(Long userId);
}
