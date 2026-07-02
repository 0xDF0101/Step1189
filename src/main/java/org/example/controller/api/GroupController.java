package org.example.controller.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.group.*;
import org.example.service.GroupService;
import org.example.utility.annotation.LoginUser;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/groups")
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<Long> createGroup(@LoginUser Long userId,
                                            @Valid @RequestBody GroupCreateRequest request) {
        return ResponseEntity.ok(groupService.createGroup(userId, request));
    }

    @GetMapping
    public ResponseEntity<List<GroupSummaryResponse>> getMyGroups(@LoginUser Long userId) {
        return ResponseEntity.ok(groupService.getMyGroups(userId));
    }

    @PostMapping("/join")
    public ResponseEntity<Long> joinGroup(@LoginUser Long userId,
                                          @Valid @RequestBody GroupJoinRequest request) {
        return ResponseEntity.ok(groupService.joinGroup(userId, request));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupDetailResponse> getGroupDetail(@LoginUser Long userId,
                                                               @PathVariable Long groupId) {
        return ResponseEntity.ok(groupService.getGroupDetail(groupId, userId));
    }

    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<Void> leaveGroup(@LoginUser Long userId,
                                           @PathVariable Long groupId) {
        groupService.leaveGroup(groupId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> deleteGroup(@LoginUser Long userId,
                                            @PathVariable Long groupId) {
        groupService.deleteGroup(groupId, userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{groupId}/goal")
    public ResponseEntity<Void> setGroupGoal(@LoginUser Long userId,
                                             @PathVariable Long groupId,
                                             @Valid @RequestBody GroupGoalRequest request) {
        groupService.setGroupGoal(groupId, userId, request);
        return ResponseEntity.ok().build();
    }
}
