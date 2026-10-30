package com.sbp.content.client.dto;

import java.util.UUID;

public record StudioClientResponse(UUID id, String name, String slug, boolean active) {
}
