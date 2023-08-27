package com.petit.toon.service.rank.response;

import com.petit.toon.service.cartoon.response.CartoonResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartoonRankResponse {
    private List<CartoonResponse> cartoons;

    public CartoonRankResponse(List<CartoonResponse> cartoons) {
        this.cartoons = cartoons;
    }
}
