package com.sbp.person.service;

import lombok.RequiredArgsConstructor;
import java.util.UUID;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sbp.common.client.FileStorageOwnerRequest;
import com.sbp.common.client.FileStorageServiceClient;
import com.sbp.common.storage.StorageVisibility;
import com.sbp.person.entity.Person;
import com.sbp.person.repository.PersonRepository;

@RequiredArgsConstructor
@Service
public class PersonMediaService {

	private static final String OWNER_SERVICE = "sbp-person-service";

	private static final String OWNER_ENTITY_TYPE = "person";

	private final PersonSupportService personSupportService;

	private final PersonRepository personRepository;

	private final FileStorageServiceClient fileStorageServiceClient;


	@Transactional
	public Person uploadProfile(UUID personId, MultipartFile file, String actor) throws java.io.IOException {
		return upload(personId, file, actor, "profile", true);
	}

	@Transactional
	public Person uploadBackdrop(UUID personId, MultipartFile file, String actor) throws java.io.IOException {
		return upload(personId, file, actor, "backdrop", false);
	}

	private Person upload(UUID personId, MultipartFile file, String actor, String purpose, boolean profile)
			throws java.io.IOException {
		Person entity = personSupportService.requirePerson(personId);
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
						personId,
						purpose,
						StorageVisibility.PUBLIC,
						actor));
		if (profile) {
			entity.setProfileFileId(fileId);
		}
		else {
			entity.setBackdropFileId(fileId);
		}
		entity.setUpdatedBy(actor);
		return personRepository.save(entity);
	}

}
