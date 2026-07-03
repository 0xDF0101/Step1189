package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.dto.follow.FollowCountResponse;
import org.example.dto.follow.FollowUserResponse;
import org.example.entity.Follow;
import org.example.entity.User;
import org.example.exception.EntityNotFoundException;
import org.example.repository.FollowRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public void follow(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new IllegalStateException("자기 자신을 팔로우할 수 없습니다.");
        }

        User follower = userRepository.getReferenceById(userId);
        User following = userRepository.findById(targetUserId)
                .orElseThrow(() -> new EntityNotFoundException("팔로우하려는 유저를 찾을 수 없습니다."));

        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new IllegalStateException("이미 팔로우 중입니다.");
        }

        followRepository.save(new Follow(follower, following));
    }

    @Transactional
    public void unfollow(Long userId, Long targetUserId) {
        User follower = userRepository.getReferenceById(userId);
        User following = userRepository.getReferenceById(targetUserId);

        followRepository.findByFollowerAndFollowing(follower, following)
                .orElseThrow(() -> new EntityNotFoundException("팔로우하고 있지 않습니다."));

        followRepository.deleteByFollowerAndFollowing(follower, following);
    }

    public List<FollowUserResponse> getFollowers(Long userId) {
        User user = userRepository.getReferenceById(userId);
        return followRepository.findByFollowing(user).stream()
                .map(f -> toResponse(user, f.getFollower()))
                .toList();
    }

    public List<FollowUserResponse> getFollowing(Long userId) {
        User user = userRepository.getReferenceById(userId);
        return followRepository.findByFollower(user).stream()
                .map(f -> toResponse(user, f.getFollowing()))
                .toList();
    }

    public FollowCountResponse getFollowCounts(Long userId) {
        User user = userRepository.getReferenceById(userId);
        return new FollowCountResponse(
                followRepository.countByFollowing(user),
                followRepository.countByFollower(user)
        );
    }

    public List<FollowUserResponse> searchUsers(Long userId, String keyword) {
        User me = userRepository.getReferenceById(userId);
        return userRepository
                .searchByUsernameOrDisplayName(keyword)
                .stream()
                .filter(u -> !u.getId().equals(userId))
                .map(u -> toResponse(me, u))
                .toList();
    }

    public boolean isMutualFollow(Long userId, Long otherUserId) {
        User a = userRepository.getReferenceById(userId);
        User b = userRepository.getReferenceById(otherUserId);
        return followRepository.existsByFollowerAndFollowing(a, b)
                && followRepository.existsByFollowerAndFollowing(b, a);
    }

    private FollowUserResponse toResponse(User me, User target) {
        boolean followedByMe = followRepository.existsByFollowerAndFollowing(me, target);
        return new FollowUserResponse(target.getId(), target.getUsername(), target.getDisplayName(), followedByMe);
    }
}
