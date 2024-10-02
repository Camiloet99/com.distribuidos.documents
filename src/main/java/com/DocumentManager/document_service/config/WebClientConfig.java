package com.DocumentManager.document_service.config;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;

import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.ALL_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Data
@Configuration
@Lazy(value = false)
public class WebClientConfig {

    @Bean
    public WebClient webClient(EnvironmentConfig config) throws SSLException {
        ExchangeStrategies exchangeStrategies = ExchangeStrategies
                .builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(1024 * 1024 * config.getMaxPayloadSizeInMb()))
                .build();

        WebClient.Builder builder = WebClient.builder();

        if (config.getSecurityDisableSslCertValidation()) {
            disableSslCertValidation(builder);
        }

        return builder
                .defaultHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .defaultHeader(ACCEPT, ALL_VALUE)
                .exchangeStrategies(exchangeStrategies)
                .build();
    }

    private void disableSslCertValidation(WebClient.Builder builder) throws SSLException {
        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

        HttpClient httpClient = HttpClient.create().secure(t -> t.sslContext(sslContext));
        builder.clientConnector(new ReactorClientHttpConnector(httpClient));
    }

}