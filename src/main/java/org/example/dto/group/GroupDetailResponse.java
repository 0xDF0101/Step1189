package org.example.dto.group;

import java.util.List;

public record GroupDetailResponse(
        Long id,
        String name,
        String description,
        String inviteCode,
        int memberCount,
        int maxMembers,
        boolean isLeader,
        List<MemberRankingDto> totalRankings,
        List<MemberRankingDto> weeklyRankings,
        List<MemberRankingDto> streakRankings,
        GroupGoalDto goal,
        List<GroupMemberDto> members
) {}
