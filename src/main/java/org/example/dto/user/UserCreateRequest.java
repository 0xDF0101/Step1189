package org.example.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @Email
        String email,
        @NotBlank(message = "이름은 공백일 수 없습니다.")
        @Size(min=4, max=20)
        String username,
        @Size(min=8, max=20)
        String password
) {
}
