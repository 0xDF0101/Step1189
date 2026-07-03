package org.example.service;

import org.example.entity.Poke;
import org.example.entity.User;
import org.example.exception.EntityNotFoundException;
import org.example.model.Role;
import org.example.repository.PokeRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 테스트 항목
 * 1. 맞팔 상태에서 정상적으로 콕 찌르기 성공
 * 2. 자기 자신을 찌르면 예외
 * 3. 대상 유저가 없으면 예외
 * 4. 맞팔이 아니면 예외
 */

@ExtendWith(MockitoExtension.class)
class PokeServiceTest {

    @Mock
    private PokeRepository pokeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FollowService followService;

    @InjectMocks
    private PokeService pokeService;

    User me;
    User target;

    @BeforeEach
    void set() {
        me = User.builder().username("me").role(Role.USER).build();
        target = User.builder().username("target").role(Role.USER).build();
    }

    @Test
    @DisplayName("맞팔 상태면 정상적으로 콕 찌르기 성공")
    void poke_success() {
        when(userRepository.getReferenceById(1L)).thenReturn(me);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(followService.isMutualFollow(1L, 2L)).thenReturn(true);

        pokeService.poke(1L, 2L);

        verify(pokeRepository).save(any(Poke.class));
    }

    @Test
    @DisplayName("자기 자신을 찌르면 예외")
    void poke_self_throws() {
        assertThrows(IllegalStateException.class, () -> pokeService.poke(1L, 1L));

        verify(pokeRepository, never()).save(any(Poke.class));
    }

    @Test
    @DisplayName("찌르려는 대상이 없으면 예외")
    void poke_target_not_found_throws() {
        when(userRepository.getReferenceById(1L)).thenReturn(me);
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> pokeService.poke(1L, 2L));

        verify(pokeRepository, never()).save(any(Poke.class));
    }

    @Test
    @DisplayName("맞팔 상태가 아니면 예외")
    void poke_not_mutual_throws() {
        when(userRepository.getReferenceById(1L)).thenReturn(me);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(followService.isMutualFollow(1L, 2L)).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> pokeService.poke(1L, 2L));

        verify(pokeRepository, never()).save(any(Poke.class));
    }
}
