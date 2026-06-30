package org.example.service;

import org.example.dto.progress.BatchRecordRequest;
import org.example.dto.progress.RecordRequest;

import java.time.LocalDate;
import java.util.Map;

public interface ProgressService {
    Map<Integer, Map<Integer, Integer>> getAllProgress(Long userId);
    void recordProgress(Long userId, RecordRequest request);
    void recordBatchProgress(Long userId, BatchRecordRequest request);
    Map<LocalDate, Integer> getDailyProgress(Long userId);
}
