package com.petit.toon.controller.search;

import com.petit.toon.controller.search.request.SearchRequest;
import com.petit.toon.service.cartoon.response.CartoonListResponse;
import com.petit.toon.service.search.SearchService;
import com.petit.toon.service.user.response.UserListResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/api/v1/search/user")
    public ResponseEntity<UserListResponse> searchUser(@Valid @ModelAttribute SearchRequest searchRequest,
                                                       @PageableDefault Pageable pageable) {
        UserListResponse response = searchService.searchUser(searchRequest.getKeyword(), pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/v1/search/toon")
    public ResponseEntity<CartoonListResponse> searchToon(@Valid @ModelAttribute SearchRequest searchRequest,
                                                          @PageableDefault Pageable pageable) {
        CartoonListResponse response = searchService.searchCartoon(searchRequest.getKeyword(), pageable);
        return ResponseEntity.ok(response);
    }
}
