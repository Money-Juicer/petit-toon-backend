package com.petit.toon.repository.cartoon;

import com.petit.toon.entity.cartoon.Like;

import java.util.List;

public interface CustomLikeRepository {

    void bulkInsert(List<Like> likes);
}
