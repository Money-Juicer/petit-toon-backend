package com.petit.toon.service.cartoon;

import com.petit.toon.repository.cartoon.ImageRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;

@SpringBootTest
@ActiveProfiles("test")
public class ImageStoreServiceTest {

    @Autowired
    ImageRepository imageRepository;

    @Autowired
    ImageStoreService imageStoreService;

    String absolutePath;
    @BeforeEach
    void setUp() {
        String path = "src/test/resources/sample-toons";
        absolutePath = new File(path).getAbsolutePath();
    }

    @Test
    @Transactional
    void 이미지저장() {

    }
}
