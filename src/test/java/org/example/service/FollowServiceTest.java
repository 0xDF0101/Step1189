package org.example.service;

import org.example.entity.Follow;
import org.example.entity.User;
import org.example.exception.EntityNotFoundException;
import org.example.model.Role;
import org.example.repository.FollowRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 테스트 항목
 * 1. 정상적으로 팔로우 되는지
 * 2. 자기 자신 팔로우 시 예외
 * 3. 중복 팔로우 시 예외
 * 4. 정상적으로 언팔로우 되는지
 * 5. 팔로우 중이 아닐 때 언팔로우 시 예외
 * 6. 맞팔 여부 판단
 */

@ExtendWith(MockitoExtension.class)
class FollowServiceTest {

    @Mock
    private FollowRepository followRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FollowService followService;

    User me;
    User target;

    @BeforeEach
    void set() {
        me = User.builder().username("me").role(Role.USER).build();
        target = User.builder().username("target").role(Role.USER).build();
    }

    @Test
    @DisplayName("정상적인 팔로우 성공")
    void follow_success() {
        when(userRepository.getReferenceById(1L)).thenReturn(me);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(followRepository.existsByFollowerAndFollowing(me, target)).thenReturn(false);

        followService.follow(1L, 2L);

        verify(followRepository).save(any(Follow.class));
    }

    @Test
    @DisplayName("자기 자신을 팔로우하면 예외")
    void follow_self_throws() {
        assertThrows(IllegalStateException.class, () -> followService.follow(1L, 1L));

        verify(followRepository, never()).save(any(Follow.class));
    }

    @Test
    @DisplayName("이미 팔로우 중이면 예외")
    void follow_duplicate_throws() {
        when(userRepository.getReferenceById(1L)).thenReturn(me);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(followRepository.existsByFollowerAndFollowing(me, target)).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> followService.follow(1L, 2L));

        verify(followRepository, never()).save(any(Follow.class));
    }

    @Test
    @DisplayName("팔로우 대상이 없으면 예외")
    void follow_target_not_found_throws() {
        when(userRepository.getReferenceById(1L)).thenReturn(me);
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> followService.follow(1L, 2L));
    }

    @Test
    @DisplayName("정상적인 언팔로우 성공")
    void unfollow_success() {
        when(userRepository.getReferenceById(1L)).thenReturn(me);
        when(userRepository.getReferenceById(2L)).thenReturn(target);
        when(followRepository.findByFollowerAndFollowing(me, target))
                .thenReturn(Optional.of(new Follow(me, target)));

        followService.unfollow(1L, 2L);

        verify(followRepository).deleteByFollowerAndFollowing(me, target);
    }

    @Test
    @DisplayName("팔로우 중이 아닌데 언팔로우 시 예외")
    void unfollow_not_following_throws() {
        when(userRepository.getReferenceById(1L)).thenReturn(me);
        when(userRepository.getReferenceById(2L)).thenReturn(target);
        when(followRepository.findByFollowerAndFollowing(me, target)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> followService.unfollow(1L, 2L));

        verify(followRepository, never()).deleteByFollowerAndFollowing(any(), any());
    }

    @Test
    @DisplayName("서로 팔로우 중이면 맞팔로 판단")
    void isMutualFollow_true() {
        when(userRepository.getReferenceById(1L)).thenReturn(me);
        when(userRepository.getReferenceById(2L)).thenReturn(target);
        when(followRepository.existsByFollowerAndFollowing(me, target)).thenReturn(true);
        when(followRepository.existsByFollowerAndFollowing(target, me)).thenReturn(true);

        assertTrue(followService.isMutualFollow(1L, 2L));
    }

    @Test
    @DisplayName("한쪽만 팔로우 중이면 맞팔 아님")
    void isMutualFollow_false() {
        when(userRepository.getReferenceById(1L)).thenReturn(me);
        when(userRepository.getReferenceById(2L)).thenReturn(target);
        when(followRepository.existsByFollowerAndFollowing(me, target)).thenReturn(true);
        when(followRepository.existsByFollowerAndFollowing(target, me)).thenReturn(false);

        assertFalse(followService.isMutualFollow(1L, 2L));
    }
}
