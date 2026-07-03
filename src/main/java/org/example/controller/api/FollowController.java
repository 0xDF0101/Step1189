package org.example.controller.api;

import lombok.RequiredArgsConstructor;
import org.example.dto.follow.FollowCountResponse;
import org.example.dto.follow.FollowUserResponse;
import org.example.service.FollowService;
import org.example.utility.annotation.LoginUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class FollowController {

    private final FollowService followService;

    @PostMapping("/follows/{targetUserId}")
    public ResponseEntity<Void> follow(@LoginUser Long userId, @PathVariable Long targetUserId) {
        followService.follow(userId, targetUserId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/follows/{targetUserId}")
    public ResponseEntity<Void> unfollow(@LoginUser Long userId, @PathVariable Long targetUserId) {
        followService.unfollow(userId, targetUserId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/follows/me/followers")
    public ResponseEntity<List<FollowUserResponse>> getMyFollowers(@LoginUser Long userId) {
        return ResponseEntity.ok(followService.getFollowers(userId));
    }

    @GetMapping("/follows/me/following")
    public ResponseEntity<List<FollowUserResponse>> getMyFollowing(@LoginUser Long userId) {
        return ResponseEntity.ok(followService.getFollowing(userId));
    }

    @GetMapping("/follows/me/counts")
    public ResponseEntity<FollowCountResponse> getMyFollowCounts(@LoginUser Long userId) {
        return ResponseEntity.ok(followService.getFollowCounts(userId));
    }

    @GetMapping("/users/search")
    public ResponseEntity<List<FollowUserResponse>> searchUsers(@LoginUser Long userId,
                                                                  @RequestParam String q) {
        return ResponseEntity.ok(followService.searchUsers(userId, q));
    }
}
