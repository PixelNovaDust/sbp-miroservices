package com.sbp.content.client.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TaxonomyClientResponse(UUID id, String type, String name, String slug, boolean active) {
}
