package org.example.controller.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.EmailDuplicateException;
import org.example.service.UserService;
import org.example.dto.user.UserCreateRequest;
import org.example.dto.user.UserInfo;
import org.example.utility.validator.UserEmailDuplicateValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated // ---> 파라미터로 넘어오는 데이터를 검사하기 위함
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * 사실 쓰지는 않음 (언젠간 쓰지 않을까? 내부적으로라도?)
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserInfo> getUser(@PathVariable Long userId) { // <<< 파라미터 검증 필요
        UserInfo userinfo = userService.getUser(userId);
        return ResponseEntity.ok().body(userinfo);
    }

    /**
     * 로컬 회원가입 요청 로직 (OAuth X)
     */
    @PostMapping("/api/v1/users")
    public ResponseEntity<Void> createUser(@Valid @RequestBody UserCreateRequest request) {

        userService.signUp(request);

        log.info("회원 등록 완료 : {}", request.username());
        return ResponseEntity.ok().build();
    }

    /**
     * OAuth로 회원가입 시 username을 따로 입력받는 컨트롤러
     */
    @PostMapping("/api/v1/users/username")
    public ResponseEntity<Void> setUsername(@NotBlank @Size(min=4, max=20) @RequestParam("username") String username,
                                            @AuthenticationPrincipal OAuth2User oAuth2User) {

        log.debug("입력받은 아이디 : {}", username);

        String email = oAuth2User.getAttribute("email");
        // --> 사용자 식별용 이메일

        userService.updateUsername(email, username);

        return ResponseEntity.ok().build();
    }
}
