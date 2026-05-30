package com.contentpublish.controller;

import com.contentpublish.common.Result;
import com.contentpublish.dto.FileUploadResultDTO;
import com.contentpublish.service.FileStorageService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;
    private final String uploadPath;

    public FileController(FileStorageService fileStorageService,
                          @org.springframework.beans.factory.annotation.Value("${storage.local.upload-path:./uploads}") String uploadPath) {
        this.fileStorageService = fileStorageService;
        this.uploadPath = uploadPath;
    }

    @PostMapping("/upload")
    public Result<FileUploadResultDTO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "IMAGE") String type) {
        return Result.ok(fileStorageService.upload(file, type));
    }

    @PostMapping("/upload/chunk")
    public Result<Void> uploadChunk(
            @RequestParam("file") MultipartFile file,
            @RequestParam("uploadId") String uploadId,
            @RequestParam("chunkIndex") int chunkIndex,
            @RequestParam("totalChunks") int totalChunks) {
        fileStorageService.uploadChunk(file, uploadId, chunkIndex, totalChunks);
        return Result.ok();
    }

    @PostMapping("/upload/merge")
    public Result<FileUploadResultDTO> merge(
            @RequestParam("uploadId") String uploadId,
            @RequestParam("fileName") String fileName,
            @RequestParam(defaultValue = "IMAGE") String type) {
        return Result.ok(fileStorageService.merge(uploadId, fileName, type));
    }

    @GetMapping("/{filename}")
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        Path filePath = Path.of(uploadPath, filename);
        if (!filePath.toFile().exists()) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = new FileSystemResource(filePath);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(resource);
    }
}
