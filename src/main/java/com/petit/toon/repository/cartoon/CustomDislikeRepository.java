package com.petit.toon.repository.cartoon;

import com.petit.toon.entity.cartoon.Dislike;

import java.util.List;

public interface CustomDislikeRepository {
    void bulkInsert(List<Dislike> dislikes);
}
