package com.euchre.platform.dto;

import jakarta.validation.constraints.NotBlank;

public record PlayCardRequest(@NotBlank String card) {
}
