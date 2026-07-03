package org.example.dto.follow;

public record FollowCountResponse(
        int followerCount,
        int followingCount
) {}
