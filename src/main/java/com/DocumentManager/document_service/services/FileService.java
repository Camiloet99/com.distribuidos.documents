package com.DocumentManager.document_service.services;

import com.DocumentManager.document_service.models.DocumentEntity;
import com.DocumentManager.document_service.repositories.DocumentRepository;
import com.DocumentManager.document_service.services.centarlizer.CentralizerFacade;
import com.google.cloud.storage.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.List;
import java.util.stream.Collectors;
import static reactor.core.publisher.Mono.just;
import java.util.stream.StreamSupport;


@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    @Value("${spring.cloud.gcp.storage.bucket}")
    private String bucketName;
    private final Storage storage = StorageOptions.getDefaultInstance().getService();
    private final KafkaProducer kafkaProducer;
    private final DocumentRepository repository;
    private final CentralizerFacade centralizerFacade;

    private DocumentEntity buildDocumentEntity(String url, String userId, String description, String name,
                                               Boolean isVerified) {
        return DocumentEntity.builder()
                .documentName(name)
                .description(description)
                .userDocumentId(Long.valueOf(userId))
                .downloadLink(url)
                .creationDate(LocalDateTime.now())
                .isVerified(isVerified)
                .build();
    }

    public Mono<List<DocumentEntity>> getDocumentsByUserId(String userId) {
        log.info("Getting documents of user " + userId);

        return repository.findByUserDocumentId(Long.valueOf(userId))
                .collectList();
    }

    public Mono<DocumentEntity> uploadFile(MultipartFile file, String userId, String description, String name,
                                           Boolean verified) {
        try {
            String folderName = "usuarios/" + userId + "/";
            String fileName = folderName + file.getOriginalFilename();
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName).build();
            storage.create(blobInfo, file.getBytes());

            // Enviar notificación de subida
            // kafkaProducer.sendNotification("Archivo subido: " + fileName);
            String url = String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);
            return repository.save(buildDocumentEntity(url, userId, description, name, verified))
                    .map(documentEntity -> documentEntity);
        } catch (IOException e) {
            throw new RuntimeException("Error subiendo archivo a GCP", e);
        }
    }

    public String uploadFile2(MultipartFile file, String userId) {
        try {
            String folderName = "usuarios/" + userId + "/";
            String fileName = folderName + file.getOriginalFilename();
            BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName).build();
            storage.create(blobInfo, file.getBytes());

            // Enviar notificación de subida
            //kafkaProducer.sendNotification("Archivo subido: " + fileName);
            return String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName);
        } catch (IOException e) {
            throw new RuntimeException("Error subiendo archivo a GCP", e);
        }
    }

    public Mono<DocumentEntity> verifyDocumentWithCentralizer(Long userId, Long documentId) {

        return repository.findByUserDocumentId(userId)
                .collectList()
                .flatMap(documentEntities -> documentEntities.stream()
                        .filter(documentEntity -> documentEntity.getDocumentId().equals(documentId))
                        .findFirst()
                        .map(documentEntity -> centralizerFacade.verifyDocument(userId, documentEntity)
                                .flatMap(isVerified -> repository.save(documentEntity.toBuilder()
                                                .isVerified(true)
                                                .build())
                                        .map(verifiedDocument -> verifiedDocument)))
                        .orElse(Mono.empty()));
    }

    public String downloadFile(String clientId, String fileName) {
        try {
            BlobId blobId = BlobId.of(bucketName, "clientes/" + clientId + "/" + fileName);
            Blob blob = storage.get(blobId);
            String tempFilePath = System.getProperty("java.io.tmpdir") + "/" + fileName;
            blob.downloadTo(Paths.get(tempFilePath));

            // Enviar notificación de descarga
            //kafkaProducer.sendNotification("Archivo descargado: " + fileName);
            return tempFilePath;
        } catch (Exception e) {
            throw new RuntimeException("Error descargando archivo de GCP", e);
        }
    }

    public Mono<String> deleteFile(Long clientId, String fileName) {
        return repository.findByUserDocumentId(clientId)
                .collectList()
                .flatMap(documentEntities -> documentEntities.stream()
                        .filter(documentEntity -> documentEntity.getDocumentName().equalsIgnoreCase(fileName))
                        .findFirst()
                        .map(documentEntity -> {
                            BlobId blobId = BlobId.of(bucketName, fileName);
                            storage.delete(blobId);

                            return repository.deleteById(documentEntity.getDocumentId())
                                    .then(just("Documento eliminado"));
                        })
                        .orElse(Mono.empty()));
    }

    private List<DocumentEntity> getEntitiesFromListUrls(List<String> urls, String userId) {
        return urls.stream()
                .map(url -> DocumentEntity.fromUrl(url, userId))
                .toList();
    }

    public Mono<List<DocumentEntity>> uploadMultipleFiles(MultipartFile[] files, String userId) {
        List<String> fileUrls = new ArrayList<>();
        try {
            String folderName = "usuarios/" + userId + "/";
            for (MultipartFile file : files) {
                String fileName = folderName + file.getOriginalFilename();
                BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName).build();
                storage.create(blobInfo, file.getBytes());
                fileUrls.add(String.format("https://storage.googleapis.com/%s/%s", bucketName, fileName));

                // Enviar notificación de subida (opcional)
                // kafkaProducer.sendNotification("Archivo subido: " + fileName);
            }

            List<DocumentEntity> documents = getEntitiesFromListUrls(fileUrls, userId);
            return repository.saveAll(documents)
                    .collectList();
        } catch (IOException e) {
            throw new RuntimeException("Error subiendo archivos a GCP", e);
        }
    }

    public List<String> downloadAllFiles(String userId) {
        List<String> filePaths = new ArrayList<>();
        String folderPath = "usuarios/" + userId + "/";

        String tempDir = System.getProperty("java.io.tmpdir") + "myapp/";
        File dir = new File(tempDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        Iterable<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(folderPath)).iterateAll();

        for (Blob blob : blobs) {
            String downloadFilePath = tempDir + blob.getName().replace(folderPath, "");

            blob.downloadTo(Paths.get(downloadFilePath));
            filePaths.add(downloadFilePath);
            // kafkaProducer.sendNotification("Archivo descargado: " + blob.getName());
        }
        return filePaths;
    }



    public String deleteAllFiles(String userId) {
        String folderPath = "usuarios/" + userId + "/";

        Iterable<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(folderPath)).iterateAll();

        for (Blob blob : blobs) {
            boolean deleted = storage.delete(blob.getBlobId());

            if (deleted) {
                // kafkaProducer.sendNotification("Archivo eliminado: " + blob.getName());
            } else {
                // kafkaProducer.sendNotification("Error al eliminar archivo: " + blob.getName());
            }
        }

        return "Todos los archivos del usuario " + userId + " han sido eliminados.";
    }

    public String downloadEspecificFile(String userId, String fileName) {
        String filePath = "usuarios/" + userId + "/" + fileName;

        String tempDir = System.getProperty("java.io.tmpdir") + "myapp/";
        File dir = new File(tempDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String downloadFilePath = tempDir + fileName;

        Blob blob = storage.get(bucketName, filePath);

        if (blob == null) {
            throw new RuntimeException("El archivo no existe en el bucket.");
        }

        blob.downloadTo(Paths.get(downloadFilePath));

        return downloadFilePath;
    }


    public String deleteEspecificFile(String userId, String fileName) {
        String filePath = "usuarios/" + userId + "/" + fileName;

        Blob blob = storage.get(bucketName, filePath);

        if (blob == null) {
            throw new RuntimeException("El archivo no existe en el bucket.");
        }

        boolean deleted = storage.delete(blob.getBlobId());

        if (deleted) {
            return "El archivo " + fileName + " ha sido eliminado.";
        } else {
            return "Error al eliminar el archivo " + fileName + ".";
        }
    }

    public List<String> listFiles(String userId) {
        String folderPath = "usuarios/" + userId + "/";

        Iterable<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix(folderPath)).iterateAll();

        List<String> fileNames = StreamSupport.stream(blobs.spliterator(), false)
                .map(Blob::getName)
                .collect(Collectors.toList());

        return fileNames;
    }

}
