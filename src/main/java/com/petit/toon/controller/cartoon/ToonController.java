package com.petit.toon.controller.cartoon;

import com.petit.toon.controller.cartoon.dto.request.ToonUploadRequest;
import com.petit.toon.service.cartoon.ToonUploadService;
import com.petit.toon.service.cartoon.dto.output.ToonUploadOutput;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class ToonController {

    private final ToonUploadService toonUploadService;

    public ToonController(ToonUploadService toonUploadService) {
        this.toonUploadService = toonUploadService;
    }

    @PostMapping("/api/v1/toon")
    public ResponseEntity<ToonUploadOutput> upload(@Valid @ModelAttribute ToonUploadRequest toonUploadRequest) throws IOException {
        ToonUploadOutput output = toonUploadService.save(toonUploadRequest.toInput());
        return new ResponseEntity<>(output, HttpStatus.CREATED);
    }

}
