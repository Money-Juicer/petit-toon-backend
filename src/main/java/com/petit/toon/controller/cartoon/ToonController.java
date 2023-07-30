package com.petit.toon.controller.cartoon;

import com.petit.toon.controller.cartoon.request.ToonUploadRequest;
import com.petit.toon.service.cartoon.ToonService;
import com.petit.toon.service.cartoon.response.ToonUploadResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
public class ToonController {

    private final ToonService toonService;

    public ToonController(ToonService toonService) {
        this.toonService = toonService;
    }

    @PostMapping("/api/v1/toon")
    public ResponseEntity<ToonUploadResponse> upload(@Valid @ModelAttribute ToonUploadRequest toonUploadRequest) throws IOException {
        ToonUploadResponse output = toonService.save(toonUploadRequest.toInput());
        return new ResponseEntity<>(output, HttpStatus.CREATED);
    }

    @DeleteMapping("/api/v1/toon/{toonId}")
    public ResponseEntity<Void> deleteToon(@PathVariable("toonId") long toonId) {
        toonService.delete(toonId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
