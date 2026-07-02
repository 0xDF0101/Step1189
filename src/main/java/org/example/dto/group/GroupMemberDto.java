package org.example.dto.group;

import java.time.LocalDateTime;

public record GroupMemberDto(
        Long userId,
        String username,
        String role,
        LocalDateTime joinedAt
) {}
