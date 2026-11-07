package com.sbp.infra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.sbp.common.storage.SbpStorageProperties;

@SpringBootApplication
@EnableConfigurationProperties(SbpStorageProperties.class)
public class SbpFileStorageServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(SbpFileStorageServiceApplication.class, args);
	}

}
