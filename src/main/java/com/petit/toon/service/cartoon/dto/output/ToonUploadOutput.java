package com.petit.toon.service.cartoon.dto.output;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.service.cartoon.dto.input.ToonUploadInput;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ToonUploadOutput {
    private Long toonId;

    public ToonUploadOutput(Long toonId) {
        this.toonId = toonId;
    }

    public ToonUploadOutput(Cartoon cartoon) {
        this.toonId = cartoon.getId();
    }
}
