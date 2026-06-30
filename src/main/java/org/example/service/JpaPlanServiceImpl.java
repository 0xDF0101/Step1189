package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.dto.plan.PlanRequest;
import org.example.dto.plan.PlanStatsResponse;
import org.example.entity.ReadingPlan;
import org.example.entity.User;
import org.example.repository.DailyProgressRepository;
import org.example.repository.ReadingPlanRepository;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JpaPlanServiceImpl implements PlanService {

    private static final int TOTAL_CHAPTERS = 1189;

    private final ReadingPlanRepository planRepository;
    private final DailyProgressRepository dailyProgressRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void savePlan(Long userId, PlanRequest request) {
        planRepository.findByUserId(userId).ifPresentOrElse(
                plan -> plan.update(request.startDate(), request.endDate()),
                () -> {
                    User user = userRepository.getReferenceById(userId);
                    planRepository.save(new ReadingPlan(user, request.startDate(), request.endDate()));
                }
        );
    }

    @Override
    public Optional<PlanStatsResponse> getStats(Long userId) {
        return planRepository.findByUserId(userId).map(plan -> buildStats(userId, plan));
    }

    private PlanStatsResponse buildStats(Long userId, ReadingPlan plan) {
        LocalDate today = LocalDate.now();
        LocalDate start = plan.getStartDate();
        LocalDate end = plan.getEndDate();

        int totalDays = (int) ChronoUnit.DAYS.between(start, end) + 1;

        int daysElapsed;
        if (today.isBefore(start)) daysElapsed = 0;
        else if (today.isAfter(end)) daysElapsed = totalDays;
        else daysElapsed = (int) ChronoUnit.DAYS.between(start, today) + 1;

        int daysRemaining = (int) Math.max(0, ChronoUnit.DAYS.between(today, end));

        int targetPerDay = (int) Math.ceil((double) TOTAL_CHAPTERS / totalDays);

        int chaptersRead = 0;
        if (!today.isBefore(start)) {
            LocalDate readEnd = today.isAfter(end) ? end : today;
            chaptersRead = dailyProgressRepository.sumChaptersInPeriod(userId, start, readEnd);
        }

        int expectedByNow = targetPerDay * daysElapsed;
        boolean onTrack = chaptersRead >= expectedByNow;
        double percentComplete = (double) chaptersRead / TOTAL_CHAPTERS * 100;

        int chaptersLeft = Math.max(0, TOTAL_CHAPTERS - chaptersRead);
        int adjustedDailyTarget = daysRemaining > 0
                ? (int) Math.ceil((double) chaptersLeft / daysRemaining)
                : chaptersLeft;

        return new PlanStatsResponse(start, end, totalDays, daysElapsed, daysRemaining,
                targetPerDay, chaptersRead, expectedByNow, onTrack, percentComplete, adjustedDailyTarget);
    }
}
