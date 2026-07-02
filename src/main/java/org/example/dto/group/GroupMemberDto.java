package org.example.dto.group;

import java.time.LocalDateTime;

public record GroupMemberDto(
        Long userId,
        String displayName,
        String role,
        LocalDateTime joinedAt
) {}
