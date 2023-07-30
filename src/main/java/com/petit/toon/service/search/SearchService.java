package com.petit.toon.service.search;

import com.petit.toon.repository.search.SearchRepository;
import com.petit.toon.service.cartoon.response.CartoonResponse;
import com.petit.toon.service.search.response.SearchResponse;
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

    public SearchResponse search(String keyword, Pageable pageable) {
        List<String> keywords = Arrays.stream(keyword.trim().split(" ")).toList();
        List<UserResponse> userResponses = getUserInfoWith(keywords, pageable);
        List<CartoonResponse> cartoonResponses = getToonInfoWith(keywords, pageable);
        return new SearchResponse(userResponses, cartoonResponses);
    }

    private List<UserResponse> getUserInfoWith(List<String> keywords, Pageable pageable) {
        return searchRepository.searchUser(keywords, pageable)
                .stream()
                .map(UserResponse::of)
                .collect(Collectors.toList());
    }

    private List<CartoonResponse> getToonInfoWith(List<String> keywords, Pageable pageable) {
        return searchRepository.searchCartoon(keywords, pageable)
                .stream()
                .map(CartoonResponse::of)
                .collect(Collectors.toList());
    }
}
