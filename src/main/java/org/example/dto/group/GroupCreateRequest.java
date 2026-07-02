package org.example.dto.group;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GroupCreateRequest(
        @NotBlank(message = "그룹 이름을 입력해주세요")
        @Size(min = 2, max = 50, message = "그룹 이름은 2~50자여야 합니다")
        String name,

        @Size(max = 200, message = "설명은 200자 이하로 입력해주세요")
        String description,

        @Min(value = 2, message = "최소 2명 이상이어야 합니다")
        @Max(value = 50, message = "최대 50명까지 가능합니다")
        int maxMembers
) {}
