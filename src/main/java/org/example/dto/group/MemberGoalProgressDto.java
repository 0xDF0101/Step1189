package org.example.dto.group;

public record MemberGoalProgressDto(
        Long userId,
        String username,
        int readInPeriod,
        int targetChapters,
        double percent
) {}
