package com.DocumentManager.document_service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Validated
@Configuration
@Lazy(value = false)
@ConfigurationProperties(prefix = "environment")
public class EnvironmentConfig {

    @NotBlank
    private String serviceName;

    @NotNull
    @Valid
    private Domains domains;

    @NotBlank
    private String operatorId;

    @NotBlank
    private String operatorName;

    @NotNull
    @Valid
    private ServiceRetry serviceRetry;

    @NotNull
    private Boolean securityDisableSslCertValidation;

    @NotNull
    private Integer maxPayloadSizeInMb;

    @Data
    @Validated
    public static class ServiceRetry {

        @NotNull
        private Integer maxAttempts;
    }

    @Data
    @Validated
    public static class Domains {

        @NotBlank
        private String centralizerDomain;

        @NotBlank
        private String notificationsDomain;

    }

}
