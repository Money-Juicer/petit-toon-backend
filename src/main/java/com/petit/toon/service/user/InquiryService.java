package com.petit.toon.service.user;

import com.petit.toon.entity.user.User;
import com.petit.toon.repository.user.UserRepository;
import com.petit.toon.service.user.response.TagExistResponse;
import com.petit.toon.service.user.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InquiryService {

    private final UserRepository userRepository;

    public UserResponse inquiryByUserId(long userId) {
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new RuntimeException("User not found. id: " + userId));
        return UserResponse.of(user);
    }

    public TagExistResponse checkTagExist(String tag) {
        Optional<User> findUser = userRepository.findByTag(tag);
        return new TagExistResponse(findUser.isPresent());
    }
}
