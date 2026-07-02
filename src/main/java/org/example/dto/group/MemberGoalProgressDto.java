package org.example.dto.group;

public record MemberGoalProgressDto(
        Long userId,
        String displayName,
        int readInPeriod,
        int targetChapters,
        double percent
) {}
