package org.example.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.entity.User;
import org.example.exception.EntityNotFoundException;
import org.example.user.dto.UserCreateRequest;
import org.example.user.dto.UserInfo;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public UserInfo getUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("해당 회원을 찾을 수 없습니다."));
        return new UserInfo(user);
    }

    public void createUser(UserCreateRequest request) {
        User user = new User(request);
        userRepository.save(user);
        log.info("저장 완료 : {}", user.getNickname());
    }
}
