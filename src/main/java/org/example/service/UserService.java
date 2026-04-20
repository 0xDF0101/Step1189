package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.EmailNotFoundException;
import org.example.model.Role;
import org.example.repository.UserRepository;
import org.example.entity.User;
import org.example.exception.EntityNotFoundException;
import org.example.dto.user.UserCreateRequest;
import org.example.dto.user.UserInfo;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserInfo getUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("해당 회원을 찾을 수 없습니다."));
        return new UserInfo(user);
    }

    // Local 회원 가입 로직
    @Transactional
    public void signUp(UserCreateRequest dto) {

        String encodedPassword = passwordEncoder.encode(dto.password());

        User user = User.builder()
                .username(dto.username())
                .password(encodedPassword)
                .email(dto.email())
                .role(Role.USER)
                .socialType("local")
                .build();

        log.debug("회원가입할 user 정보 : {}", user);

        userRepository.save(user);

        log.info("user 저장 완료");
    }

    // OAuth로 회원가입 시, username 입력받고 업데이트 하기
    @Transactional
    public void updateUsername(String email, String username) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new EmailNotFoundException("해당 이메일이 없는데?"));

        user.updateUsernameAndRole(username, Role.USER);
        // ----> 더티 체킹
    }



}
