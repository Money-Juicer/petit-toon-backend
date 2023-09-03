package com.petit.toon.service.cartoon;

import com.petit.toon.entity.cartoon.Cartoon;
import com.petit.toon.entity.cartoon.Image;
import com.petit.toon.repository.cartoon.ImageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.imgscalr.Scalr;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
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

    private final ImageRepository imageRepository;

    public Image storeImage(MultipartFile multipartFile, Cartoon cartoon, int n, String toonDirectory) throws IOException {
        if (multipartFile.isEmpty()) {
            return null;
        }
        String originalFileName = multipartFile.getOriginalFilename();
        String storeFileName = createFileName(originalFileName, cartoon.getId(), n);
        String storePath = getFullPath(storeFileName, cartoon.getId(), toonDirectory);
        multipartFile.transferTo(new File(storePath));
        return imageRepository.save(Image.builder()
                .cartoon(cartoon)
                .fileName(storeFileName)
                .originalFileName(originalFileName)
                .path(storePath.substring(storePath.lastIndexOf("toons")))
                .build());
    }

    public List<Image> storeImages(List<MultipartFile> multipartFiles, Cartoon cartoon, String toonDirectory) throws IOException {
        createToonDirectory(cartoon.getId(), toonDirectory);
        List<Image> images = new ArrayList<>();

        for (int i = 0; i < multipartFiles.size(); i++) {
            images.add(storeImage(multipartFiles.get(i), cartoon, i, toonDirectory));
        }
        return images;
    }

    public String makeThumbnail(File file, Cartoon cartoon, String toonDirectory) throws IOException {
        BufferedImage inputImage = ImageIO.read(file);

        int width = inputImage.getWidth() / 5;
        int height = inputImage.getHeight() / 5;

        String extension = extractExtension(file.getName());
        String fileName = cartoon.getId() + "-" + "thumb" + "." + extension;
        String thumbnailPath = getFullPath(fileName, cartoon.getId(), toonDirectory);
        File thumbnailFile = new File(thumbnailPath);

        BufferedImage resizeImage = Scalr.resize(inputImage, width, height);
        ImageIO.write(resizeImage, extension, thumbnailFile);
        return thumbnailPath.substring(thumbnailPath.lastIndexOf("toons"));
    }

    public boolean deleteFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        return file.delete();
    }

    public int getLastImageNumber(Cartoon cartoon) {
        return cartoon.getImages().stream()
                .mapToInt(this::extractImageNumber)
                .max().getAsInt();
    }

    public String getFullPath(MultipartFile file, long cartoonId, int index, String toonDirectory) {
        String extension = extractExtension(file.getName());
        return toonDirectory + File.separator + cartoonId + File.separator + cartoonId + "-" + index + "." + extension;
    }

    private int extractImageNumber(Image image) {
        return Integer.parseInt(image.getPath().split("-|\\.")[1]);
    }

    private String createFileName(String originalFileName, long toonId, int n) {
        String extension = extractExtension(originalFileName);
        return toonId + "-" + n + "." + extension;
    }

    private String extractExtension(String fileName) {
        int idx = fileName.lastIndexOf(".");
        return fileName.substring(idx + 1);
    }

    private String getFullPath(String filename, Long toonId, String toonDirectory) {
        String absolutePath = toonDirectory + "/" + toonId + "/" + filename;
        return new File(absolutePath).getAbsolutePath();
    }

    private String createToonDirectory(Long toonId, String toonDirectory) throws IOException {
        String toonImageDirectory = toonDirectory + "/" + toonId;
        String toonDirectoryPath = new File(toonImageDirectory).getAbsolutePath();
        createDirectory(Path.of(toonDirectoryPath));
        return toonImageDirectory;
    }


    /**
     * deleteImages를 사용하는 서비스가 없기에 추후 수정 예정.
     *
     * ToonService에서 사용할 경우, 아래와 같이 location을 인자로 보내줌.
     * @param location > String "{app.toon.dir}/toonId"
     */
    public void deleteImagesByToonDirectory(String location) {
        String toonDirectoryPath = new File(location).getAbsolutePath();

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
