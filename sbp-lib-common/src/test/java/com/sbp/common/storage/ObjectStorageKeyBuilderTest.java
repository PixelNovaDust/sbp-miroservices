package com.sbp.common.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ObjectStorageKeyBuilderTest {

	@Test
	void buildIncludesPrefixAndSanitizedFilename() {
		String key = ObjectStorageKeyBuilder.build("dev/", "person-service/PERSON/abc/PROFILE_IMAGE", "../../evil name.jpg");
		assertTrue(key.startsWith("dev/person-service/PERSON/abc/PROFILE_IMAGE/"));
		assertTrue(key.endsWith("-evil-name.jpg") || key.contains("-evil-name.jpg"));
	}

	@Test
	void objectReferenceUsesProviderScheme() {
		assertEquals("s3://my-bucket/path/key.jpg", ObjectStorageKeyBuilder.objectReference("S3", "my-bucket", "path/key.jpg"));
	}

}
