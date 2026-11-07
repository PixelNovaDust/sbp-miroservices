package com.sbp.filestorage.controller;

import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.sbp.common.response.ApiResponse;
import com.sbp.common.response.ApiResponseFactory;
import com.sbp.filestorage.dto.DownloadUrlResponse;
import com.sbp.filestorage.dto.FileRegisterRequest;
import com.sbp.filestorage.dto.FileReferenceRegisterRequest;
import com.sbp.filestorage.dto.FileStorageResponse;
import com.sbp.filestorage.dto.PresignUploadResponse;
import com.sbp.filestorage.service.FileStorageService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor

@RequestMapping("/api/v1/files")
public class FileStorageController {

	private final FileStorageService fileStorageService;


	@GetMapping("/{fileId}")
	public ResponseEntity<ApiResponse<FileStorageResponse>> get(@PathVariable UUID fileId, HttpServletRequest request) {
		FileStorageResponse data = fileStorageService.get(fileId);
		return ResponseEntity.ok(success("FILE_FETCHED", "File metadata retrieved", data, request));
	}

	@GetMapping
	public ResponseEntity<ApiResponse<List<FileStorageResponse>>> listByOwner(
			@RequestParam String ownerEntityType,
			@RequestParam UUID ownerEntityId,
			HttpServletRequest request) {
		List<FileStorageResponse> data = fileStorageService.listByOwner(ownerEntityType, ownerEntityId);
		return ResponseEntity.ok(success("FILES_LISTED", "Files retrieved", data, request));
	}

	@PostMapping("/register-reference")
	public ResponseEntity<ApiResponse<FileStorageResponse>> registerReference(
			@Valid @RequestBody FileReferenceRegisterRequest body,
			HttpServletRequest request) {
		FileStorageResponse data = fileStorageService.registerReference(body);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(success("FILE_REFERENCE_REGISTERED", "File reference registered", data, request));
	}

	@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ApiResponse<FileStorageResponse>> upload(
			@RequestPart("file") MultipartFile file,
			@Valid @RequestPart("metadata") FileRegisterRequest metadata,
			HttpServletRequest request) throws java.io.IOException {
		FileStorageResponse data = fileStorageService.upload(file, metadata);
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(success("FILE_UPLOADED", "File uploaded", data, request));
	}

	@PostMapping("/presign-upload")
	public ResponseEntity<ApiResponse<PresignUploadResponse>> presignUpload(
			@Valid @RequestBody PresignUploadBody body,
			HttpServletRequest request) {
		PresignUploadResponse data = fileStorageService.presignUpload(
				body.filename(),
				body.contentLength(),
				body.contentType(),
				body.metadata());
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(success("FILE_PRESIGN_UPLOAD", "Presigned upload URL created", data, request));
	}

	@PostMapping("/{fileId}/confirm")
	public ResponseEntity<ApiResponse<FileStorageResponse>> confirm(
			@PathVariable UUID fileId,
			@RequestParam String actor,
			HttpServletRequest request) {
		FileStorageResponse data = fileStorageService.confirmUpload(fileId, actor);
		return ResponseEntity.ok(success("FILE_CONFIRMED", "Upload confirmed", data, request));
	}

	@GetMapping("/{fileId}/download-url")
	public ResponseEntity<ApiResponse<DownloadUrlResponse>> downloadUrl(
			@PathVariable UUID fileId,
			HttpServletRequest request) {
		DownloadUrlResponse data = fileStorageService.downloadUrl(fileId);
		return ResponseEntity.ok(success("FILE_DOWNLOAD_URL", "Download URL created", data, request));
	}

	@DeleteMapping("/{fileId}")
	public ResponseEntity<ApiResponse<Void>> delete(
			@PathVariable UUID fileId,
			@RequestParam String actor,
			HttpServletRequest request) {
		fileStorageService.softDelete(fileId, actor);
		return ResponseEntity.ok(success("FILE_DELETED", "File deleted", null, request));
	}

	public record PresignUploadBody(
			String filename,
			long contentLength,
			String contentType,
			@Valid FileRegisterRequest metadata) {
	}

	private static <T> ApiResponse<T> success(String code, String message, T data, HttpServletRequest request) {
		return ApiResponseFactory.success(code, message, data, request.getRequestURI(), request.getHeader("X-Trace-Id"));
	}

}
