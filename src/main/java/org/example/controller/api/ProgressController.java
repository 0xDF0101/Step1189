package org.example.controller.api;

import lombok.RequiredArgsConstructor;
import org.example.dto.user.CustomUserDetails;
import org.example.service.ProgressService;
import org.example.dto.progress.RecordRequest;
import org.example.utility.annotation.LoginUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    // 모든 장의 모든 진척도 요청
    @GetMapping("/progress")
    public ResponseEntity<Map<Integer, Map<Integer, Integer>>> getProgress(
            @LoginUser Long userId
    ) {
        // <권, <장, 읽은 횟수>>
        Map<Integer, Map<Integer, Integer>> allProgress = progressService.getAllProgress(userId);

        return ResponseEntity.ok().body(allProgress);
    }

    // 해당 장을 '읽음' 표시
    @PostMapping("/progress")
    public ResponseEntity<Void> recordProgress(
            @LoginUser Long userId,
            @RequestBody RecordRequest request
    ) {

        progressService.recordProgress(userId, request);
        return ResponseEntity.ok().build();
    }

    // 하루에 읽은 양을 가져옴
    @GetMapping("/main")
    public ResponseEntity<Map<LocalDate, Integer>> getDailyProgress(
            @LoginUser Long userId
    ) {
        Map<LocalDate, Integer> result = progressService.getDailyProgress(userId);
        return ResponseEntity.ok().body(result);
    }
}
