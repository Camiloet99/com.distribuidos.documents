package com.DocumentManager.document_service.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDateTime;

@Value
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DocumentRequest {

    Long documentId;
    String description;
    String name;
    Boolean isVerified;
    String downloadLink;
    Long userDocumentId;

    public static DocumentEntity toEntity(DocumentRequest request, String downloadUri) {
        return DocumentEntity.builder()
                .description(request.getDescription())
                .creationDate(LocalDateTime.now())
                .documentName(request.getName())
                .downloadLink(downloadUri)
                .userDocumentId(request.getUserDocumentId())
                .isVerified(request.getIsVerified())
                .build();
    }

}
