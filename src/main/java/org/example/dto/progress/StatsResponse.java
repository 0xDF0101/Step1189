package org.example.dto.progress;

public record StatsResponse(
        int totalRead,
        int totalChapters,
        int oldTestamentRead,
        int oldTestamentTotal,
        int newTestamentRead,
        int newTestamentTotal,
        int todayCount,
        int streak
) {
}
