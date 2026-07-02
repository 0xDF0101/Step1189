package org.example.dto.group;

import jakarta.validation.constraints.NotBlank;

public record GroupJoinRequest(
        @NotBlank(message = "초대 코드를 입력해주세요")
        String inviteCode
) {}
