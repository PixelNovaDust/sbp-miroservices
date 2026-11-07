package com.sbp.infra.dto;

import java.net.URL;
import java.time.Instant;

public record DownloadUrlResponse(URL downloadUrl, Instant expiresAt) {
}
