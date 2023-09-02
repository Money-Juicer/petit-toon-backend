package com.petit.toon.service.search;

import com.petit.toon.repository.search.SearchRepository;
import com.petit.toon.service.cartoon.response.CartoonListResponse;
import com.petit.toon.service.cartoon.response.CartoonResponse;
import com.petit.toon.service.user.response.UserListResponse;
import com.petit.toon.service.user.response.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {
    private final SearchRepository searchRepository;

    public UserListResponse searchUser(String keyword, Pageable pageable) {
        List<String> keywords = Arrays.stream(keyword.trim().split(" ")).toList();
        List<UserResponse> userResponses = getUserInfoWith(keywords, pageable);
        return new UserListResponse(userResponses);
    }

    public CartoonListResponse searchCartoon(String keyword, Pageable pageable) {
        List<String> keywords = Arrays.stream(keyword.trim().split(" ")).toList();
        List<CartoonResponse> cartoonResponses = getToonInfoWith(keywords, pageable);
        return new CartoonListResponse(cartoonResponses);
    }

    private List<UserResponse> getUserInfoWith(List<String> keywords, Pageable pageable) {
        return searchRepository.findUser(keywords, pageable)
                .stream()
                .map(UserResponse::of)
                .collect(Collectors.toList());
    }

    private List<CartoonResponse> getToonInfoWith(List<String> keywords, Pageable pageable) {
        return searchRepository.findCartoon(keywords, pageable)
                .stream()
                .map(CartoonResponse::of)
                .collect(Collectors.toList());
    }
}
