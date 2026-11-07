package com.sbp.content.service;

import lombok.RequiredArgsConstructor;
import java.util.UUID;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sbp.common.client.FileStorageOwnerRequest;
import com.sbp.common.client.FileStorageServiceClient;
import com.sbp.common.storage.StorageVisibility;
import com.sbp.content.entity.Content;
import com.sbp.content.repository.ContentRepository;

@RequiredArgsConstructor
@Service
public class ContentMediaService {

	private static final String OWNER_SERVICE = "sbp-content-service";

	private static final String OWNER_ENTITY_TYPE = "content";

	private final ContentSupportService contentSupportService;

	private final ContentRepository contentRepository;

	private final FileStorageServiceClient fileStorageServiceClient;


	@Transactional
	public Content uploadPoster(UUID contentId, MultipartFile file, String actor) throws java.io.IOException {
		return upload(contentId, file, actor, "poster", true);
	}

	@Transactional
	public Content uploadBackdrop(UUID contentId, MultipartFile file, String actor) throws java.io.IOException {
		return upload(contentId, file, actor, "backdrop", false);
	}

	private Content upload(UUID contentId, MultipartFile file, String actor, String purpose, boolean poster)
			throws java.io.IOException {
		Content entity = contentSupportService.requireContent(contentId);
		String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : purpose;
		ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
			@Override
			public String getFilename() {
				return filename;
			}
		};
		UUID fileId = fileStorageServiceClient.upload(
				resource,
				filename,
				new FileStorageOwnerRequest(
						OWNER_SERVICE,
						OWNER_ENTITY_TYPE,
						contentId,
						purpose,
						StorageVisibility.PUBLIC,
						actor));
		if (poster) {
			entity.setPosterFileId(fileId);
		}
		else {
			entity.setBackdropFileId(fileId);
		}
		entity.setUpdatedAt(java.time.Instant.now());
		return contentRepository.save(entity);
	}

}
