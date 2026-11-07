package com.sbp.common.client;

import java.net.URI;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.client.RestClient;

import com.sbp.common.response.ApiResponse;
import com.sbp.common.storage.StorageVisibility;

/**
 * HTTP client for {@code sbp-file-storage-service} (upload + download URL). Domain services use this
 * instead of talking to S3 directly.
 */
public class FileStorageServiceClient {

	private static final ParameterizedTypeReference<ApiResponse<FileStorageClientResponse>> FILE_TYPE =
			ApiClientSupport.apiResponseType(FileStorageClientResponse.class);

	private static final ParameterizedTypeReference<ApiResponse<DownloadUrlClientResponse>> DOWNLOAD_TYPE =
			ApiClientSupport.apiResponseType(DownloadUrlClientResponse.class);

	private final RestClient restClient;

	public FileStorageServiceClient(String baseUrl, RestClient.Builder restClientBuilder) {
		String normalized = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
		this.restClient = restClientBuilder.baseUrl(normalized).build();
	}

	public UUID upload(Resource file, String filename, FileStorageOwnerRequest owner) {
		MultipartBodyBuilder builder = new MultipartBodyBuilder();
		builder.part("file", file).filename(filename);
		builder.part("metadata", toMetadata(owner)).contentType(MediaType.APPLICATION_JSON);

		ApiResponse<FileStorageClientResponse> response = restClient.post()
				.uri("/api/v1/files/upload")
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.body(builder.build())
				.retrieve()
				.body(FILE_TYPE);
		FileStorageClientResponse data = ApiClientSupport.requireData(response);
		if (data == null || data.fileId() == null) {
			throw new IllegalStateException("File storage upload returned no fileId");
		}
		return data.fileId();
	}

	public URI resolveDownloadUrl(UUID fileId) {
		if (fileId == null) {
			return null;
		}
		ApiResponse<DownloadUrlClientResponse> response = restClient.get()
				.uri("/api/v1/files/{fileId}/download-url", fileId)
				.retrieve()
				.body(DOWNLOAD_TYPE);
		DownloadUrlClientResponse data = ApiClientSupport.requireData(response);
		if (data == null || data.downloadUrl() == null) {
			return null;
		}
		try {
			return data.downloadUrl().toURI();
		}
		catch (java.net.URISyntaxException ex) {
			throw new IllegalStateException("Invalid download URL from file storage", ex);
		}
	}

	private static FileStorageMetadataPart toMetadata(FileStorageOwnerRequest owner) {
		return new FileStorageMetadataPart(
				owner.ownerService(),
				owner.ownerEntityType(),
				owner.ownerEntityId(),
				owner.purpose(),
				owner.visibility() != null ? owner.visibility() : StorageVisibility.PRIVATE,
				owner.actor());
	}

	public record FileStorageClientResponse(UUID fileId) {
	}

	public record DownloadUrlClientResponse(java.net.URL downloadUrl, java.time.Instant expiresAt) {
	}

	private record FileStorageMetadataPart(
			String ownerService,
			String ownerEntityType,
			UUID ownerEntityId,
			String purpose,
			StorageVisibility visibility,
			String actor) {
	}

}
