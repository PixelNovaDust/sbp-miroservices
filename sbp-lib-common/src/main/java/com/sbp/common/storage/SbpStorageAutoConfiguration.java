package com.sbp.common.storage;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.sbp.common.storage.s3.S3ClientSupport;
import com.sbp.common.storage.s3.S3ObjectStorageClient;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@AutoConfiguration
@ConditionalOnClass(S3Client.class)
@ConditionalOnProperty(prefix = "sbp.storage", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(SbpStorageProperties.class)
public class SbpStorageAutoConfiguration {

	@Bean(destroyMethod = "close")
	@ConditionalOnMissingBean
	S3Client sbpS3Client(SbpStorageProperties properties) {
		return S3ClientSupport.createClient(properties);
	}

	@Bean(destroyMethod = "close")
	@ConditionalOnMissingBean
	S3Presigner sbpS3Presigner(SbpStorageProperties properties) {
		return S3ClientSupport.createPresigner(properties);
	}

	@Bean
	@ConditionalOnMissingBean
	ObjectStorageClient objectStorageClient(S3Client s3Client, S3Presigner presigner, SbpStorageProperties properties) {
		return new S3ObjectStorageClient(s3Client, presigner, properties);
	}

}
