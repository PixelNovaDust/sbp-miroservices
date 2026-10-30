package com.sbp.person.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record PersonStudioRequest(@NotNull UUID studioId, @NotNull UUID relationshipTypeId) {
}
