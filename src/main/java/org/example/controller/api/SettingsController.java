package org.example.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.user.PasswordChangeRequest;
import org.example.dto.user.UserInfo;
import org.example.dto.user.UsernameChangeRequest;
import org.example.service.UserService;
import org.example.utility.annotation.LoginUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class SettingsController {

    private final UserService userService;

    @GetMapping("/api/v1/settings/me")
    public ResponseEntity<UserInfo> getMe(@LoginUser Long userId) {
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PatchMapping("/api/v1/settings/username")
    public ResponseEntity<Void> changeUsername(@LoginUser Long userId,
                                               @Valid @RequestBody UsernameChangeRequest request) {
        userService.changeUsername(userId, request.username());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/api/v1/settings/password")
    public ResponseEntity<Void> changePassword(@LoginUser Long userId,
                                               @Valid @RequestBody PasswordChangeRequest request) {
        userService.changePassword(userId, request.currentPassword(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/v1/settings/account")
    public ResponseEntity<Void> deleteAccount(@LoginUser Long userId, HttpServletRequest request) {
        userService.deleteAccount(userId);
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok().build();
    }
}
