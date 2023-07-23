package com.petit.toon.controller.cartoon;

import com.petit.toon.controller.cartoon.dto.request.ToonUploadRequest;
import com.petit.toon.controller.cartoon.dto.response.ToonUploadResponse;
import com.petit.toon.service.cartoon.ToonUploadService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ToonController {

    private final ToonUploadService toonUploadService;

    public ToonController(ToonUploadService toonUploadService) {
        this.toonUploadService = toonUploadService;
    }

    @PostMapping("/api/v1/toon")
    public ResponseEntity<ToonUploadResponse> upload(@Valid @ModelAttribute ToonUploadRequest toonUploadRequest) {
        Long output = toonUploadService.save(toonUploadRequest.toInput());
        ToonUploadResponse response = new ToonUploadResponse(output);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

}
