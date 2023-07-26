package com.petit.toon.service.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.cartoon.Image;
import com.petit.toon.repository.cartoon.ImageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.Files.createDirectory;

@Service
@Transactional
@RequiredArgsConstructor
public class ImageService {

    @Value("${app.toon.dir}")
    private String location;

    private final ImageRepository imageRepository;

    public Image storeImage(MultipartFile multipartFile, Cartoon cartoon, int n) throws IOException {
        if (multipartFile.isEmpty()) {
            return null;
        }
        String originalFileName = multipartFile.getOriginalFilename();
        String storeFileName = createFileName(originalFileName, cartoon.getId() + 1, n);
        String storePath = getFullPath(storeFileName, cartoon.getId() + 1);
        multipartFile.transferTo(new File(storePath));
        Image img = Image.builder()
                .cartoon(cartoon)
                .fileName(storeFileName)
                .originalFileName(originalFileName)
                .path(storePath)
                .build();
        return img;
    }

    public List<Image> storeImages(List<MultipartFile> multipartFiles, Cartoon cartoon) throws IOException {
        createToonDirectory(cartoon.getId() + 1);
        List<Image> images = new ArrayList<>();

        for (int i = 0; i < multipartFiles.size(); i++) {
            images.add(storeImage(multipartFiles.get(i), cartoon, i));
        }
        return images;
    }

    private String createFileName(String originalFileName, long toonId, int n) {
        String extension = extractExtension(originalFileName);
        return toonId + "-" + n + "." + extension;
    }

    private String extractExtension(String fileName) {
        int idx = fileName.lastIndexOf(".");
        return fileName.substring(idx + 1);
    }

    private String getFullPath(String filename, Long toonId) {
        String absolutePath = location + "/" + toonId + "/" + filename;
        return new File(absolutePath).getAbsolutePath();
    }

    private void createToonDirectory(Long toonId) throws IOException {
        String absolutePath = location + "/" + toonId;
        String toonDirectoryPath = new File(absolutePath).getAbsolutePath();
        createDirectory(Path.of(toonDirectoryPath));
    }

    public void deleteImages(Long toonId) {
        String absolutePath = location + "/" + toonId;
        String toonDirectoryPath = new File(absolutePath).getAbsolutePath();

        File directory = new File(toonDirectoryPath);
        try {
            while (directory.exists()) {
                File[] listFiles = directory.listFiles();

                for (File file : listFiles) {
                    file.delete();
                }

                if (listFiles.length == 0 && directory.isDirectory()) {
                    directory.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}