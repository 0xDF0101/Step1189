package org.example.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsernameChangeRequest(
        @NotBlank(message = "닉네임을 입력해주세요")
        @Size(min = 4, max = 20, message = "닉네임은 4~20자여야 합니다")
        String username
) {}
