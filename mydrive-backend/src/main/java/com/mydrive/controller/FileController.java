package com.mydrive.controller;

import com.mydrive.dto.response.FileResponse;
import com.mydrive.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    // POST /api/files/upload
    @PostMapping("/upload")
    public ResponseEntity<FileResponse> upload(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) throws IOException {

        FileResponse response = fileService.upload(userDetails.getUsername(), file);
        return ResponseEntity.status(201).body(response);
    }

    // GET /api/files
    @GetMapping
    public ResponseEntity<List<FileResponse>> getMyFiles(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(fileService.getMyFiles(userDetails.getUsername()));
    }

    // GET /api/files/{id}
    @GetMapping("/{id}")
    public ResponseEntity<FileResponse> getDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        return ResponseEntity.ok(fileService.getDetail(userDetails.getUsername(), id));
    }

    // GET /api/files/{id}/download
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) throws MalformedURLException {

        Resource resource = fileService.download(userDetails.getUsername(), id);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    // DELETE /api/files/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {

        fileService.delete(userDetails.getUsername(), id);
        return ResponseEntity.noContent().build();
    }

    // GET /api/files/{id}/download-shared
    @GetMapping("/{id}/download-shared")
    public ResponseEntity<Resource> downloadShared(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) throws MalformedURLException {

        Resource resource = fileService.downloadShared(userDetails.getUsername(), id);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}