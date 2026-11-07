package com.sbp.infra;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.sbp.common.storage.ObjectStorageClient;

@SpringBootTest
@ActiveProfiles("test")
class SbpFileStorageServiceApplicationTests {

	@MockitoBean
	ObjectStorageClient objectStorageClient;

	@Test
	void contextLoads() {
	}

}
