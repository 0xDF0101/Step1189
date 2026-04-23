package org.example.service;

import org.example.dto.user.UserCreateRequest;
import org.example.entity.User;
import org.example.exception.EmailDuplicateException;
import org.example.exception.EmailNotFoundException;
import org.example.exception.EntityNotFoundException;
import org.example.exception.UsernameDuplicateException;
import org.example.model.Role;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 테스트 항목
 * 1. 정상적으로 signUp 되는지
 * 2. 이메일 중복 시 예외 터지는지
 * 3. 사용자 이름 중복 시 예외 터지는지
 * 4. 동일한 User 객체가 생성이 되는지
 */

@ExtendWith(MockitoExtension.class)
class UserServiceTest {


    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;


    UserCreateRequest dto;
    String email = "email@email.com";
    String username = "username";
    String password = "password123";
    @BeforeEach
    void set() {
        dto = new UserCreateRequest(email, username, password);
    }


    @Test
    @DisplayName("정상적인 sign up 성공")
    void signUp_success() {
        when(userRepository.existsUserByEmail(email)).thenReturn(false);
        when(userRepository.existsUserByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        userService.signUp(dto);

        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("이메일 중복시 예외")
    void duplicate_email() {
        when(userRepository.existsUserByEmail(email)).thenReturn(true);

        assertThrows(EmailDuplicateException.class, () -> {
            userService.signUp(dto);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("사용자 이름 중복시 예외")
    void duplicate_username() {
        when(userRepository.existsUserByEmail(email)).thenReturn(false);
        when(userRepository.existsUserByUsername(username)).thenReturn(true);

        assertThrows(UsernameDuplicateException.class, () -> {
            userService.signUp(dto);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("User 객체 생성")
    void user_construct() {
        when(userRepository.existsUserByEmail(email)).thenReturn(false);
        when(userRepository.existsUserByUsername(username)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");

        User expectedUser = User.builder()
                .username(username)
                .password("encodedPassword")
                .email(email)
                .role(Role.USER)
                .socialType("local")
                .build();
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        userService.signUp(dto);

        verify(userRepository).save(userCaptor.capture());

        User actualUser = userCaptor.getValue();

        assertAll(
                () -> assertEquals(expectedUser.getEmail(), actualUser.getEmail()),
                () -> assertEquals(expectedUser.getUsername(), actualUser.getUsername()),
                () -> assertEquals(expectedUser.getPassword(), actualUser.getPassword()),
                () -> assertEquals(expectedUser.getRole(), actualUser.getRole()),
                () -> assertEquals(expectedUser.getSocialType(), actualUser.getSocialType())
        );

    }

    /**
     * updateUsername(String email, String username)
     * 1. 잘 저장 되는지
     * 2. 중복 시 예외 발생하는지
     * 3. User 객체가 잘 생성되는지 -> 생성이 아니라 좀 다름
     * 4. 이메일을 찾지 못했을때 예외가 터지는지
     */

    @Test
    @DisplayName("updateUsername 저장 성공")
    void updateUsername_success() {

        User storedUser = User.builder()
                .username(null)
                .password("encodedPassword")
                .email(email)
                .role(Role.USER)
                .socialType("local")
                .build();

        when(userRepository.existsUserByUsername("new-username")).thenReturn(false);
        when(userRepository.findByEmail(email)).thenReturn(Optional.ofNullable(storedUser));

        userService.updateUsername(email, "new-username");

        assertEquals("new-username", storedUser.getUsername());
        // 더티 체킹은 확인 못하므로 그냥 비즈니스 로직만 검증
    }

    @Test
    @DisplayName("username 중복 시 예외")
    void duplicate_username_test() {
        when(userRepository.existsUserByUsername(username)).thenReturn(true);

        assertThrows(UsernameDuplicateException.class, () -> {
            userService.updateUsername(email, username);
        });
    }

    @Test
    @DisplayName("email을 찾지 못했을 때 예외")
    void not_found_email() {
        when(userRepository.existsUserByUsername(username)).thenReturn(false);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(EmailNotFoundException.class, () -> {
            userService.updateUsername(email, username);
        });
    }

    @Test
    @DisplayName("getUser 작동 확인")
    void getUser_test() {

        User user = User.builder()
                .username(username)
                .password("encodedPassword")
                .email(email)
                .role(Role.USER)
                .socialType("local")
                .build();

        when(userRepository.findById(anyLong())).thenReturn(Optional.ofNullable(user));

        userService.getUser(1L);

        verify(userRepository).findById(anyLong());
    }

    @Test
    @DisplayName("찾는 user가 없을 시 예외")
    void not_found_user() {
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            userService.getUser(1L);
        });

    }
}



















