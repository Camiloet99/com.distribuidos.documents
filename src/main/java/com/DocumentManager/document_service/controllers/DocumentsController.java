package com.DocumentManager.document_service.controllers;

import com.DocumentManager.document_service.models.DocumentEntity;
import com.DocumentManager.document_service.models.ResponseBody;
import com.DocumentManager.document_service.services.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/files")
public class DocumentsController {
    @Autowired
    private FileService fileService;

    @PostMapping("/upload/{userId}")
    public Mono<ResponseEntity<ResponseBody<DocumentEntity>>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @PathVariable String userId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "verified", required = false) Boolean isVerified) {

        return fileService.uploadFile(file, userId, description, name, isVerified)
                .map(ControllerUtils::created);
    }

    @PostMapping("/upload2/{userId}")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, @PathVariable String userId) {
        String fileUrl = fileService.uploadFile2(file, userId);
        return new ResponseEntity<>(fileUrl, HttpStatus.OK);
    }

    @PostMapping("/verify/user/{userId}/documentId/{documentId}")
    public Mono<ResponseEntity<ResponseBody<DocumentEntity>>> verifyDocumentWithCentralizer(
            @PathVariable Long userId,
            @PathVariable Long documentId) {

        return fileService.verifyDocumentWithCentralizer(userId, documentId)
                .map(ControllerUtils::ok);
    }

    @GetMapping("/list/{userId}")
    public Mono<ResponseEntity<ResponseBody<List<DocumentEntity>>>> getDocumentsByUserId(
            @PathVariable String userId) {

        return fileService.getDocumentsByUserId(userId)
                .map(ControllerUtils::ok);
    }

    @GetMapping("/download/{userId}/{fileName}")
    public ResponseEntity<ResponseBody<String>> downloadFile(@PathVariable String userId, @PathVariable String fileName) {

        return ControllerUtils.ok(fileService.downloadFile(fileName, userId));
    }

    @DeleteMapping("/delete/{userId}/{fileName}")
    public Mono<ResponseEntity<ResponseBody<String>>> deleteFile(@PathVariable Long userId,
                                                                 @PathVariable String fileName) {

        return fileService.deleteFile(userId, fileName)
                .map(ControllerUtils::ok);
    }

    // Método para subir múltiples archivos
    @PostMapping("/upload/all/{userId}")
    public ResponseEntity<List<String>> uploadFiles(@RequestParam("files") MultipartFile[] files, @PathVariable String userId) {
        List<String> fileUrls = fileService.uploadMultipleFiles(files, userId);
        return new ResponseEntity<>(fileUrls, HttpStatus.OK);
    }

    @GetMapping("/download/all/{userId}")
    public ResponseEntity<List<String>> downloadAllFiles(@PathVariable String userId) {

        List<String> filePaths = fileService.downloadAllFiles(userId);
        return new ResponseEntity<>(filePaths, HttpStatus.OK);
    }

    @DeleteMapping("/delete/all/{userId}")
    public ResponseEntity<String> deleteAllFiles(@PathVariable String userId) {
        String result = fileService.deleteAllFiles(userId);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
