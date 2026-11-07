package com.sbp.common.client;

import java.net.URI;
import java.util.UUID;

public final class MediaUrlSupport {

	private MediaUrlSupport() {
	}

	public static String resolveOrNull(FileStorageServiceClient client, UUID fileId) {
		if (fileId == null || client == null) {
			return null;
		}
		try {
			URI uri = client.resolveDownloadUrl(fileId);
			return uri != null ? uri.toString() : null;
		}
		catch (RuntimeException ex) {
			return null;
		}
	}

}
