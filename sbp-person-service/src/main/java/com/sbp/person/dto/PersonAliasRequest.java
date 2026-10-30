package com.sbp.person.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PersonAliasRequest(@NotBlank @Size(max = 255) String alias) {
}
