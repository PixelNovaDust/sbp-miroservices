package com.sbp.person.dto;

import java.time.Instant;
import java.util.UUID;

public record PersonAliasResponse(UUID id, String alias, Instant createdAt) {
}
