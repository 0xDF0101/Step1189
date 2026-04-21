package org.example.controller.api;

import jakarta.validation.Valid;
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
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final UserEmailDuplicateValidator validator;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(validator);
    }
    // --> 검증

    /**
     * 사실 쓰지는 않음 (언젠간 쓰지 않을까? 내부적으로라도?)
     */
    @ResponseBody
    @GetMapping("/users/{userId}")
    public ResponseEntity<UserInfo> getUser(@PathVariable Long userId) { // <<< 파라미터 검증 필요
        UserInfo userinfo = userService.getUser(userId);
        return ResponseEntity.ok().body(userinfo);
    }

    /**
     * 로컬 회원가입 요청 로직 (OAuth X)
     */
    @ResponseBody
    @PostMapping("/api/v1/users")
    public ResponseEntity<Void> createUser(@Valid @RequestBody UserCreateRequest request,
                                           BindingResult bindingResult) {
        if(bindingResult.hasErrors()) {
            throw new EmailDuplicateException(bindingResult);
        }

        userService.signUp(request);

        log.info("회원 등록 완료 : {}", request.username());
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
