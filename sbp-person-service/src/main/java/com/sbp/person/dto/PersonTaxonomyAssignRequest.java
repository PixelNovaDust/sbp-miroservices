package com.sbp.person.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record PersonTaxonomyAssignRequest(@NotNull UUID taxonomyId) {
}
