package com.contentpublish.service.impl;

import com.contentpublish.dto.FileUploadResultDTO;
import com.contentpublish.entity.UploadFile;
import com.contentpublish.mapper.UploadFileMapper;
import com.contentpublish.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.*;
import java.util.UUID;

@Service
public class LocalFileStorageServiceImpl implements FileStorageService {

    @Value("${storage.local.upload-path:./uploads}")
    private String uploadPath;

    private final UploadFileMapper uploadFileMapper;

    public LocalFileStorageServiceImpl(UploadFileMapper uploadFileMapper) {
        this.uploadFileMapper = uploadFileMapper;
    }

    @Override
    public FileUploadResultDTO upload(MultipartFile file, String type) {
        try {
            Files.createDirectories(Path.of(uploadPath));
            String ext = getExtension(file.getOriginalFilename());
            String storedName = UUID.randomUUID() + "." + ext;
            Path dest = Path.of(uploadPath, storedName);
            file.transferTo(dest.toFile());

            FileUploadResultDTO result = new FileUploadResultDTO();
            result.setUrl("/api/files/" + storedName);
            result.setFileSize(file.getSize());

            if ("IMAGE".equalsIgnoreCase(type)) {
                try {
                    BufferedImage img = ImageIO.read(dest.toFile());
                    if (img != null) {
                        result.setWidth(img.getWidth());
                        result.setHeight(img.getHeight());
                    }
                } catch (Exception ignored) {}
            }

            saveUploadFile(file.getOriginalFilename(), storedName, type, file.getSize());
            return result;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public void uploadChunk(MultipartFile file, String uploadId, int chunkIndex, int totalChunks) {
        try {
            Path chunkDir = Path.of(uploadPath, "chunks", uploadId);
            Files.createDirectories(chunkDir);
            Path chunkFile = chunkDir.resolve("chunk_" + chunkIndex);
            file.transferTo(chunkFile.toFile());
        } catch (IOException e) {
            throw new RuntimeException("分片上传失败: " + e.getMessage());
        }
    }

    @Override
    public FileUploadResultDTO merge(String uploadId, String fileName, String type) {
        try {
            Path chunkDir = Path.of(uploadPath, "chunks", uploadId);
            String ext = getExtension(fileName);
            String storedName = UUID.randomUUID() + "." + ext;
            Path dest = Path.of(uploadPath, storedName);

            try (OutputStream out = Files.newOutputStream(dest)) {
                for (int i = 0; ; i++) {
                    Path chunkFile = chunkDir.resolve("chunk_" + i);
                    if (!Files.exists(chunkFile)) break;
                    Files.copy(chunkFile, out);
                }
            }

            // 清理分片目录
            try {
                Files.walk(chunkDir).sorted(java.util.Comparator.reverseOrder())
                    .forEach(p -> { try { Files.delete(p); } catch (Exception ignored) {} });
            } catch (Exception ignored) {}

            long fileSize = Files.size(dest);
            FileUploadResultDTO result = new FileUploadResultDTO();
            result.setUrl("/api/files/" + storedName);
            result.setFileSize(fileSize);

            if ("IMAGE".equalsIgnoreCase(type)) {
                try {
                    BufferedImage img = ImageIO.read(dest.toFile());
                    if (img != null) {
                        result.setWidth(img.getWidth());
                        result.setHeight(img.getHeight());
                    }
                } catch (Exception ignored) {}
            }

            saveUploadFile(fileName, storedName, type, fileSize);
            return result;
        } catch (IOException e) {
            throw new RuntimeException("文件合并失败: " + e.getMessage());
        }
    }

    private void saveUploadFile(String originalName, String storedName, String type, long size) {
        UploadFile uf = new UploadFile();
        uf.setOriginalName(originalName);
        uf.setStoredPath(storedName);
        uf.setFileType(type.toUpperCase());
        uf.setFileSize(size);
        uf.setUploadStatus("READY");
        uploadFileMapper.insert(uf);
    }

    private String getExtension(String filename) {
        if (filename == null) return "bin";
        int i = filename.lastIndexOf('.');
        return i >= 0 ? filename.substring(i + 1) : "bin";
    }
}
