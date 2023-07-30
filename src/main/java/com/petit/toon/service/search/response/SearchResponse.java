package com.petit.toon.service.search.response;

import com.petit.toon.service.cartoon.response.CartoonResponse;
import com.petit.toon.service.user.response.UserResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class SearchResponse {
    private List<UserResponse> users;
    private List<CartoonResponse> toons;

    public SearchResponse(List<UserResponse> users, List<CartoonResponse> toons) {
        this.users = users;
        this.toons = toons;
    }
}
