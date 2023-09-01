package com.petit.toon.service.cartoon.event;

import lombok.Getter;

@Getter
public class CartoonUploadedEvent {

    private long authorId;
    private long cartoonId;

    public CartoonUploadedEvent(long authorId, long cartoonId) {
        this.authorId = authorId;
        this.cartoonId = cartoonId;
    }
}
