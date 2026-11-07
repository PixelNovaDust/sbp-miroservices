package com.sbp.common.storage.s3;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import com.sbp.common.storage.DownloadRequest;
import com.sbp.common.storage.ObjectHead;
import com.sbp.common.storage.ObjectStorageClient;
import com.sbp.common.storage.ObjectStorageException;
import com.sbp.common.storage.ObjectStorageKeyBuilder;
import com.sbp.common.storage.PresignUploadRequest;
import com.sbp.common.storage.PresignedUpload;
import com.sbp.common.storage.SbpStorageProperties;
import com.sbp.common.storage.StorageMimeTypes;
import com.sbp.common.storage.StorageProvider;
import com.sbp.common.storage.UploadRequest;
import com.sbp.common.storage.UploadResult;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class S3ObjectStorageClient implements ObjectStorageClient {

	private final S3Client s3Client;

	private final S3Presigner presigner;

	private final SbpStorageProperties properties;

	public S3ObjectStorageClient(S3Client s3Client, S3Presigner presigner, SbpStorageProperties properties) {
		this.s3Client = s3Client;
		this.presigner = presigner;
		this.properties = properties;
	}

	@Override
	public UploadResult upload(UploadRequest request) {
		String bucket = resolveBucket(request.bucket());
		String objectKey = resolveObjectKey(request.objectKey(), request.keyPrefix(), request.filename());
		String mimeType = resolveContentType(request.contentType(), request.filename());
		try {
			PutObjectRequest.Builder putBuilder = PutObjectRequest.builder()
					.bucket(bucket)
					.key(objectKey)
					.contentType(mimeType);
			RequestBody body = request.contentLength() >= 0
					? RequestBody.fromInputStream(request.content(), request.contentLength())
					: RequestBody.fromInputStream(request.content(), 0);
			PutObjectResponse response = s3Client.putObject(putBuilder.build(), body);
			long size = request.contentLength() >= 0 ? request.contentLength() : 0;
			String objectReference = ObjectStorageKeyBuilder.objectReference(
					properties.getProvider().name(), bucket, objectKey);
			log.info(
					"Uploaded object bucket={} objectReference={} sizeBytes={} contentType={}",
					bucket,
					objectReference,
					size,
					mimeType);
			return new UploadResult(
					bucket,
					objectKey,
					objectReference,
					response.eTag(),
					size,
					mimeType,
					properties.getProvider(),
					properties.getRegion());
		}
		catch (Exception ex) {
			throw new ObjectStorageException("Upload failed for " + bucket + "/" + objectKey, ex);
		}
	}

	@Override
	public PresignedUpload presignUpload(PresignUploadRequest request) {
		String bucket = resolveBucket(request.bucket());
		String objectKey = resolveObjectKey(request.objectKey(), request.keyPrefix(), request.filename());
		String mimeType = resolveContentType(request.contentType(), request.filename());
		Duration ttl = properties.getPresignUploadTtl();
		try {
			PutObjectRequest objectRequest = PutObjectRequest.builder()
					.bucket(bucket)
					.key(objectKey)
					.contentType(mimeType)
					.build();
			PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
					.signatureDuration(ttl)
					.putObjectRequest(objectRequest)
					.build();
			PresignedPutObjectRequest presigned = presigner.presignPutObject(presignRequest);
			return new PresignedUpload(
					presigned.url(),
					bucket,
					objectKey,
					ObjectStorageKeyBuilder.objectReference(properties.getProvider().name(), bucket, objectKey),
					mimeType,
					Instant.now().plus(ttl),
					Map.copyOf(presigned.signedHeaders()));
		}
		catch (Exception ex) {
			throw new ObjectStorageException("Presign upload failed for " + bucket + "/" + objectKey, ex);
		}
	}

	@Override
	public URL presignDownload(DownloadRequest request) {
		Duration ttl = properties.getPresignDownloadTtl();
		try {
			GetObjectRequest getRequest = GetObjectRequest.builder()
					.bucket(request.bucket())
					.key(request.objectKey())
					.build();
			GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
					.signatureDuration(ttl)
					.getObjectRequest(getRequest)
					.build();
			PresignedGetObjectRequest presigned = presigner.presignGetObject(presignRequest);
			return presigned.url();
		}
		catch (Exception ex) {
			throw new ObjectStorageException("Presign download failed for " + request.bucket() + "/" + request.objectKey(), ex);
		}
	}

	@Override
	public ObjectHead head(String bucket, String objectKey) {
		try {
			HeadObjectResponse response = s3Client.headObject(HeadObjectRequest.builder()
					.bucket(bucket)
					.key(objectKey)
					.build());
			return new ObjectHead(bucket, objectKey, response.contentLength(), response.eTag(), response.contentType());
		}
		catch (Exception ex) {
			throw new ObjectStorageException("Head failed for " + bucket + "/" + objectKey, ex);
		}
	}

	@Override
	public void delete(String bucket, String objectKey) {
		try {
			s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(objectKey).build());
			String objectReference = ObjectStorageKeyBuilder.objectReference(
					properties.getProvider().name(), bucket, objectKey);
			log.info("Deleted object bucket={} objectReference={}", bucket, objectReference);
		}
		catch (Exception ex) {
			throw new ObjectStorageException("Delete failed for " + bucket + "/" + objectKey, ex);
		}
	}

	private String resolveBucket(String bucketOverride) {
		String bucket = bucketOverride != null && !bucketOverride.isBlank()
				? bucketOverride
				: properties.getDefaultBucket();
		if (bucket == null || bucket.isBlank()) {
			throw new ObjectStorageException("No bucket configured (set sbp.storage.default-bucket or pass bucket on request)");
		}
		return bucket;
	}

	private static String resolveContentType(String override, String filename) {
		if (override != null && !override.isBlank()) {
			return override;
		}
		return StorageMimeTypes.fromFilename(filename);
	}

	private String resolveObjectKey(String explicitKey, String keyPrefix, String filename) {
		if (explicitKey != null && !explicitKey.isBlank()) {
			return explicitKey;
		}
		return ObjectStorageKeyBuilder.build(properties.getKeyPrefix(), keyPrefix, filename);
	}

}
