package org.example.dto.follow;

public record FollowUserResponse(
        Long userId,
        String username,
        String displayName,
        boolean followedByMe // 현재 로그인한 유저가 이 사람을 팔로우 중인지
) {}
