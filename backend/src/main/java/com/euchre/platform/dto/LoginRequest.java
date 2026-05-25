package com.euchre.platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank
        @Size(min = 3, max = 20)
        @Pattern(regexp = "^[A-Za-z0-9_]+$")
        String username
) {
}
