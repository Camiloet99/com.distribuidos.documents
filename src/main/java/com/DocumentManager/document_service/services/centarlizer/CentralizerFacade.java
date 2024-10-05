package com.DocumentManager.document_service.services.centarlizer;

import com.DocumentManager.document_service.config.EnvironmentConfig;
import com.DocumentManager.document_service.models.DocumentEntity;
import com.DocumentManager.document_service.services.centarlizer.models.VerifyDocumentRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import static com.DocumentManager.document_service.exceptions.ErrorCodes.CENTRALIZER_UPSTREAM_ERROR;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;

@Slf4j
@Component
@AllArgsConstructor
public class CentralizerFacade {

    private final WebClient webClient;
    private final EnvironmentConfig environmentConfig;

    private static final String VERIFY_DOCUMENT = "/authenticateDocument";

    private VerifyDocumentRequest getVerifyDocumentRequest(Long citizenId, DocumentEntity entity) {
        return VerifyDocumentRequest.builder()
                .citizenDocument(citizenId)
                .documentTitle(entity.getDescription())
                .urlDocument(entity.getDownloadLink())
                .build();
    }

    public Mono<Boolean> verifyDocument(Long citizenId, DocumentEntity documentEntity) {

        val resourceUri = environmentConfig.getDomains().getCentralizerDomain() + VERIFY_DOCUMENT;
        VerifyDocumentRequest request = getVerifyDocumentRequest(citizenId, documentEntity);

        log.info("verificando documento " + resourceUri.toLowerCase());
        return webClient
                .method(HttpMethod.PUT)
                .uri(resourceUri)
                .bodyValue(request)
                .exchangeToMono(verifyDocumentResponse -> {

                    HttpStatus httpStatus = HttpStatus.valueOf(verifyDocumentResponse.statusCode().value());

                    if (HttpStatus.OK.equals(httpStatus)) {
                        log.info("Document validated successfully");
                        return just(true);
                    }

                    HttpHeaders responseHeaders = verifyDocumentResponse.headers().asHttpHeaders();
                    return verifyDocumentResponse.bodyToMono(String.class)
                            .flatMap(responseBody -> {
                                log.error("{} - The centralizer service responded with "
                                                + "an unexpected failure response for: {}"
                                                + "\nStatus Code: {}\nResponse Headers: {}\nResponse Body: {}",
                                        CENTRALIZER_UPSTREAM_ERROR, resourceUri, httpStatus, responseHeaders,
                                        responseBody);
                                return error(new RuntimeException(responseBody));
                            });
                })
                .retryWhen(Retry
                        .max(environmentConfig.getServiceRetry().getMaxAttempts())
                        .filter(RuntimeException.class::isInstance)
                        .onRetryExhaustedThrow((ignore1, ignore2) -> ignore2.failure()));
    }

}
