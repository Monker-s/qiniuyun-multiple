package com.contentpublish.service;

import com.contentpublish.dto.FileUploadResultDTO;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    FileUploadResultDTO upload(MultipartFile file, String type);
    void uploadChunk(MultipartFile file, String uploadId, int chunkIndex, int totalChunks);
    FileUploadResultDTO merge(String uploadId, String fileName, String type);
}
