package com.petit.toon.repository.user;

import com.petit.toon.entity.user.User;

import java.util.List;

public interface CustomUserRepository {

    List<User> findAllWithProfileImageWithExactOrder(List<Long> ids);
}
