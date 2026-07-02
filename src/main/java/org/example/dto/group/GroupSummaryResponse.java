package org.example.dto.group;

public record GroupSummaryResponse(
        Long id,
        String name,
        String description,
        String inviteCode,
        int memberCount,
        int maxMembers,
        String myRole
) {}
