package org.example.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsernameChangeRequest(
        @NotBlank(message = "아이디를 입력해주세요")
        @Size(min = 4, max = 20, message = "아이디는 4~20자여야 합니다")
        @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "영문, 숫자, -, _만 사용할 수 있습니다")
        String username
) {}
