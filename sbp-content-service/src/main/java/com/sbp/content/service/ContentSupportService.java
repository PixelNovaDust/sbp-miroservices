package com.sbp.content.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.sbp.content.entity.Content;
import com.sbp.content.exception.ResourceNotFoundException;
import com.sbp.content.repository.ContentRepository;

@Service
public class ContentSupportService {

	private final ContentRepository contentRepository;

	public ContentSupportService(ContentRepository contentRepository) {
		this.contentRepository = contentRepository;
	}

	public Content requireContent(UUID contentId) {
		return contentRepository.findById(contentId)
				.orElseThrow(() -> new ResourceNotFoundException("Content not found: " + contentId));
	}

}
