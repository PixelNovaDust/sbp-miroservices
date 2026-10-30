package com.sbp.studio.dto;

import java.time.Instant;

public record StudioAuditResponse(Instant createdAt, Instant updatedAt, int version) {
}
