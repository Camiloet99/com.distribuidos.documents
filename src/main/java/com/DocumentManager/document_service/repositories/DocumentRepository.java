package com.DocumentManager.document_service.repositories;

import com.DocumentManager.document_service.models.DocumentEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface DocumentRepository extends R2dbcRepository<DocumentEntity, Long> {

    Flux<DocumentEntity> findByUserDocumentId(Long documentId);

}
