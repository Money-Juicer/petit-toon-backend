package com.petit.toon.repository.user;

import com.petit.toon.entity.user.Follow;

import java.util.List;

public interface CustomFollowRepository {
    void bulkInsert(List<Follow> follows);
}
