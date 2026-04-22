package org.example.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @Email
        String email,
        @Size(min=4, max=20)
        String username,
        @Size(min=4, max=20)
        String password
) {
}
