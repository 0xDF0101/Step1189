package org.example.service.auth;

import org.example.config.SecurityConfig;
import org.example.entity.User;
import org.example.exception.EmailNotFoundException;
import org.example.exception.EntityNotFoundException;
import org.example.model.Role;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Import(SecurityConfig.class)
class CustomUserDetailServiceTest {

    @InjectMocks
    private CustomUserDetailService customUserDetailService;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("해당 username이 존재하지 않을 경우 예외처리")
    void not_found_exception() {
        String username = "unknown_user";

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            customUserDetailService.loadUserByUsername(username);
        });
    }

    @Test
    @DisplayName("해당 username의 사용자가 존재하면 CustomUserDetails을 반환")
    void return_customUserDetails() {
        String username = "testuser";
        User user = User.builder()
                        .username(username)
                        .role(Role.USER)
                        .email("example@example.com")
                        .build();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailService.loadUserByUsername(username);

        assertEquals(result.getUsername(), user.getEmail());
        assertTrue(result.getAuthorities().toString().contains("USER"));
    }

}