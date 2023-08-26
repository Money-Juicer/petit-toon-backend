package com.petit.toon.service.cartoon.response;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartoonListResponse {
    private List<CartoonResponse> cartoons;

    public CartoonListResponse(List<CartoonResponse> cartoons) {
        this.cartoons = cartoons;
    }
}
