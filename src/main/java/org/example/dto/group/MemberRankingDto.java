package org.example.dto.group;

public record MemberRankingDto(
        Long userId,
        String username,
        int value
) {}
