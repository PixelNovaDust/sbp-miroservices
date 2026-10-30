package com.sbp.person.client.dto;

import java.util.UUID;

public record EntityTaxonomyAssignClientRequest(UUID entityId, String entityType, UUID taxonomyId) {
}
