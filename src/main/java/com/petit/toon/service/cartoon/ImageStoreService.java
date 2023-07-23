package com.petit.toon.service.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.cartoon.Image;
import com.petit.toon.repository.cartoon.ImageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@PropertySource("classpath:test.properties")
public class ImageStoreService {
//    @Value("${app.image.dir}")
    private String location = "src/test/resources/images";

    private final ImageRepository imageRepository;

    public Image storeImage(MultipartFile multipartFile, Cartoon cartoon, short n) throws IOException {
        if (multipartFile.isEmpty()) {
            return null;
        }
        String originalFileName = multipartFile.getOriginalFilename();
        String storeFileName = createFileName(originalFileName, cartoon.getId(), n);
        String storePath = getFullPath(storeFileName, cartoon.getId());
        multipartFile.transferTo(new File(storePath));
        return imageRepository.save(Image.builder()
                .cartoon(cartoon)
                .fileName(storeFileName)
                .originalFileName(originalFileName)
                .path(storePath)
                .build());
    }

    public List<Image> storeImages(List<MultipartFile> multipartFiles, Cartoon cartoon) {
        int n = 0;
        List<Image> images = new ArrayList<>();
        multipartFiles.stream()
                .filter(it -> !it.isEmpty())
                .forEach(it -> {
                    try {
                        images.add(storeImage(it, cartoon, (short) (n + 1)));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        return images;
    }

//    public List<Image> findImagesByIds(List<Long> imageId) {
//        return imageId.stream()
//                .map(id -> imageRepository.findById(id)
//                        .orElseThrow(
//                                IdNotFoundException::new
//                        )).collect(Collectors.toList());
//    }

    private String createFileName(String originalFileName, long toonId, short n) {
        String extension = extractExtension(originalFileName);
        return toonId + "-" + n + "." + extension;
    }

    private String extractExtension(String fileName) {
        int idx = fileName.lastIndexOf(".");
        return fileName.substring(idx + 1);
    }

    private String getFullPath(String filename, Long toonId) {
        String absolutePath = new File(location).getAbsolutePath();
        return absolutePath + "/" + toonId + "/" + filename;
    }
}