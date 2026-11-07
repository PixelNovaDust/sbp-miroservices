package com.sbp.common.storage.s3;

import java.net.URI;

import com.sbp.common.storage.SbpStorageProperties;
import com.sbp.common.storage.StorageProvider;

import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

public final class S3ClientSupport {

	private S3ClientSupport() {
	}

	public static S3Client createClient(SbpStorageProperties properties) {
		var builder = S3Client.builder()
				.region(Region.of(properties.getRegion()))
				.credentialsProvider(DefaultCredentialsProvider.create());
		applyEndpoint(builder, properties);
		return builder.build();
	}

	public static S3Presigner createPresigner(SbpStorageProperties properties) {
		var builder = S3Presigner.builder()
				.region(Region.of(properties.getRegion()))
				.credentialsProvider(DefaultCredentialsProvider.create());
		if (properties.getEndpoint() != null && !properties.getEndpoint().isBlank()) {
			builder.endpointOverride(URI.create(properties.getEndpoint()));
		}
		builder.serviceConfiguration(S3Configuration.builder()
				.pathStyleAccessEnabled(resolvePathStyle(properties))
				.build());
		return builder.build();
	}

	private static void applyEndpoint(software.amazon.awssdk.services.s3.S3ClientBuilder builder, SbpStorageProperties properties) {
		if (properties.getEndpoint() != null && !properties.getEndpoint().isBlank()) {
			builder.endpointOverride(URI.create(properties.getEndpoint()));
		}
		builder.serviceConfiguration(S3Configuration.builder()
				.pathStyleAccessEnabled(resolvePathStyle(properties))
				.build());
	}

	private static boolean resolvePathStyle(SbpStorageProperties properties) {
		if (properties.isPathStyleAccess()) {
			return true;
		}
		return properties.getProvider() == StorageProvider.MINIO;
	}

}
