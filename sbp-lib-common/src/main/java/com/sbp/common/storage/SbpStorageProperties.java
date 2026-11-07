package com.sbp.common.storage;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "sbp.storage")
@Getter
@Setter
public class SbpStorageProperties {

	private boolean enabled = false;

	private StorageProvider provider = StorageProvider.S3;

	private String region = "us-east-1";

	private String defaultBucket;

	private String endpoint = "";

	private String keyPrefix = "";

	private boolean pathStyleAccess = false;

	private Duration presignUploadTtl = Duration.ofMinutes(15);

	private Duration presignDownloadTtl = Duration.ofHours(1);

}
