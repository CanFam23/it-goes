package com.it_goes.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AwsConfig {
    @Bean
    S3Presigner s3Presigner() {
        // Region + credentials resolve from default AWS profile (~/.aws/*)
        return S3Presigner.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }
}