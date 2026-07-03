package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.EmailDuplicateException;
import org.example.exception.EmailNotFoundException;
import org.example.exception.InvalidPasswordException;
import org.example.exception.UsernameDuplicateException;
import org.example.model.Role;
import org.example.repository.DailyProgressRepository;
import org.example.repository.FollowRepository;
import org.example.repository.GroupMemberRepository;
import org.example.repository.PokeRepository;
import org.example.repository.ReadingPlanRepository;
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
    private final ReadingPlanRepository readingPlanRepository;
    private final DailyProgressRepository dailyProgressRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final FollowRepository followRepository;
    private final PokeRepository pokeRepository;

    public UserInfo getUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("해당 회원을 찾을 수 없습니다."));
        return new UserInfo(user);
    }

    // Local 회원 가입 로직
    @Transactional
    public void signUp(UserCreateRequest dto) {
        // 비즈니스 로직에 대한 검증은 여기서 해야한다!
        if(userRepository.existsUserByEmail(dto.email())) {
            throw new EmailDuplicateException("이미 사용 중인 이메일입니다.");
        }
        if(userRepository.existsUserByUsername(dto.username())) {
            throw new UsernameDuplicateException("이미 사용 중인 사용자 이름입니다.");
        }

        String encodedPassword = passwordEncoder.encode(dto.password());

        User user = User.builder()
                .username(dto.username())
                .displayName(dto.displayName())
                .password(encodedPassword)
                .email(dto.email())
                .role(Role.USER)
                .socialType("local")
                .build();

        userRepository.save(user);
        log.info("user 저장 완료 : {}", user.getId());
    }

    // OAuth 회원가입 1단계: @handle 설정
    @Transactional
    public void updateUsername(Long userId, String username) {
        if (userRepository.existsUserByUsername(username)) {
            throw new UsernameDuplicateException("이미 사용 중인 아이디입니다.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저가 없습니다."));
        user.updateUsername(username);
    }

    // OAuth 회원가입 2단계: 표시 이름 설정 + USER 승격
    @Transactional
    public void setDisplayName(Long userId, String displayName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저가 없습니다."));
        user.updateDisplayName(displayName);
        user.promoteToUser();
    }

    // 설정: @handle 변경 (중복 체크 있음)
    @Transactional
    public void changeUsername(Long userId, String username) {
        if (userRepository.existsUserByUsername(username)) {
            throw new UsernameDuplicateException("이미 사용 중인 아이디입니다.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저가 없습니다."));
        user.updateUsername(username);
    }

    // 설정: 표시 이름 변경 (중복 체크 없음)
    @Transactional
    public void changeDisplayName(Long userId, String displayName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저가 없습니다."));
        user.updateDisplayName(displayName);
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저가 없습니다."));
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new InvalidPasswordException("현재 비밀번호가 일치하지 않습니다.");
        }
        user.updatePassword(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public void deleteAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("해당 유저가 없습니다."));
        groupMemberRepository.deleteByUser(user);
        readingPlanRepository.deleteByUser(user);
        dailyProgressRepository.deleteByUser(user);
        followRepository.deleteByFollower(user);
        followRepository.deleteByFollowing(user);
        pokeRepository.deleteBySender(user);
        pokeRepository.deleteByReceiver(user);
        userRepository.delete(user);
    }
}
