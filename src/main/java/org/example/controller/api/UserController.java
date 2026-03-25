package org.example.controller.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.UserService;
import org.example.dto.user.UserCreateRequest;
import org.example.dto.user.UserInfo;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @ResponseBody
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserInfo> getUser(@PathVariable Long userId) { // <<< 파라미터 검증 필요

        UserInfo userinfo = userService.getUser(userId);

        return ResponseEntity.ok().body(userinfo);
    }

//    @PostMapping("/users")
//    public ResponseEntity<Void> createUser(UserCreateRequest request) {
//
//        if(request == null) {
//            throw new IllegalArgumentException(); // <<<< 커스텀 예외처리 하기
//        }

//        userService.createUser(request);
//
//        log.info("회원 등록 완료 : ", request.nickname());
//        return ResponseEntity.ok().build();
//    }

    @ResponseBody
    @PostMapping("/api/v1/users")
    public ResponseEntity<Void> createUser(@RequestBody UserCreateRequest request) {

        if(request == null) {
            throw new IllegalArgumentException(); // <<<< 커스텀 예외처리 하기
        }

        userService.signUp(request);

        log.info("회원 등록 완료 : ", request.username());
        return ResponseEntity.ok().build();
    }

    // 얘는 그냥 Controller니까 클래스 분리해도 될 듯
    @PostMapping("/api/v1/users/username")
    public String setUsername(@RequestParam("username") String username,
                                            @AuthenticationPrincipal OAuth2User oAuth2User) {

        log.debug("입력받은 아이디 : {}", username);

        String email = oAuth2User.getAttribute("email");
        // --> 사용자 식별용 이메일

        userService.updateUsername(email, username);

        return "redirect:/main";
    }






}
