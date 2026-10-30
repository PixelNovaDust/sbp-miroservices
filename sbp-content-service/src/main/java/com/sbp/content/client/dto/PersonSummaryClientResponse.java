package com.sbp.content.client.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PersonSummaryClientResponse(UUID id, String name, boolean active) {
}
