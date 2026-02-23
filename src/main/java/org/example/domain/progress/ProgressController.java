package org.example.domain.progress;

import lombok.RequiredArgsConstructor;
import org.example.domain.progress.dto.RecordRequest;
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
            @RequestHeader(value = "X-USER-ID", defaultValue = "1") Long userId
    ) {
        Map<Integer, Map<Integer, Integer>> allProgress = progressService.getAllProgress(userId);

        return ResponseEntity.ok().body(allProgress);
    }

    // 해당 장을 '읽음' 표시
    @PostMapping("/progress")
    public ResponseEntity<Void> recordProgress(
            @RequestHeader(value = "X-USER-ID", defaultValue = "1") Long userId,
            @RequestBody RecordRequest request
    ) {

        progressService.recordProgress(userId, request);
        return ResponseEntity.ok().build();
    }

    // 하루에 읽은 양을 가져옴
    @GetMapping("/main")
    public ResponseEntity<Map<LocalDate, Integer>> getDailyProgress(
            @RequestHeader(value = "X-USER-ID", defaultValue = "1") Long userId
    ) {
        Map<LocalDate, Integer> result = progressService.getDailyProgress(userId);
        return ResponseEntity.ok().body(result);
    }



}
