package com.DocumentManager.document_service.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
@Table(name = "documents")
public class DocumentEntity {

    @Id
    @Column("id")
    Long documentId;

    @Column("name")
    String documentName;

    @Column("description")
    String description;

    @Column("verified")
    Boolean isVerified;

    @Column("download_link")
    String downloadLink;

    @Column("user_document_id")
    Long userDocumentId;

    @Column("creation_date")
    LocalDateTime creationDate;

    public static DocumentEntity fromUrl(String url, String userId) {
        return DocumentEntity.builder()
                .creationDate(LocalDateTime.now())
                .downloadLink(url)
                .userDocumentId(Long.valueOf(userId))
                .isVerified(false)
                .build();
    }


}
