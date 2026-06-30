package org.example.controller.api;

import lombok.RequiredArgsConstructor;
import org.example.service.ProgressService;
import org.example.dto.progress.BatchRecordRequest;
import org.example.dto.progress.RecordRequest;
import org.example.dto.progress.StatsResponse;
import org.example.utility.annotation.LoginUser;
import org.springframework.http.ResponseEntity;
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

    // 여러 장을 한번에 '읽음' 표시
    @PostMapping("/progress/batch")
    public ResponseEntity<Void> recordBatchProgress(
            @LoginUser Long userId,
            @RequestBody BatchRecordRequest request
    ) {
        progressService.recordBatchProgress(userId, request);
        return ResponseEntity.ok().build();
    }

    // 여러 장의 읽기 기록 취소
    @PostMapping("/progress/batch/cancel")
    public ResponseEntity<Void> cancelBatchProgress(
            @LoginUser Long userId,
            @RequestBody BatchRecordRequest request
    ) {
        progressService.cancelBatchProgress(userId, request);
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

    // 통계 조회
    @GetMapping("/stats")
    public ResponseEntity<StatsResponse> getStats(
            @LoginUser Long userId
    ) {
        return ResponseEntity.ok().body(progressService.getStats(userId));
    }
}
