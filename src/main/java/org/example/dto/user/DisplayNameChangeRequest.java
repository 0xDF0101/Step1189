package org.example.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DisplayNameChangeRequest(
        @NotBlank(message = "표시 이름을 입력해주세요")
        @Size(min = 2, max = 20, message = "표시 이름은 2~20자여야 합니다")
        String displayName
) {}
