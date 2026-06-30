package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.repository.BibleRepository;
import org.example.repository.DailyProgressRepository;
import org.example.repository.ProgressRepository;
import org.example.dto.progress.BatchRecordRequest;
import org.example.dto.progress.DailyProgressDto;
import org.example.dto.progress.RecordRequest;
import org.example.dto.progress.StatsResponse;
import org.example.repository.UserRepository;
import org.example.entity.Bible;
import org.example.entity.Progress;
import org.example.entity.DailyProgress;
import org.example.entity.User;
import org.example.exception.EntityNotFoundException;
import org.example.model.Testament;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 현재는 JPA를 통해서 직접 DB에 저장된다.
 * 추후에 Redis를 통해서 캐싱할 수 있도록 한다.
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class JpaProgressServiceImpl implements ProgressService {

    private final ProgressRepository progressRepository;
    private final DailyProgressRepository dailyProgressRepository;
    private final UserRepository userRepository;
    private final BibleRepository bibleRepository;

    public Map<Integer, Map<Integer, Integer>> getAllProgress(Long userId) {

        Map<Integer, Map<Integer, Integer>> allProgress = new HashMap<>();
        // <권, <장, 읽은 횟수>>
        List<Progress> progressList = progressRepository.findAllByUserId(userId);
        // ---> 읽었던 모든 권을 가져옴

        for(Progress progress : progressList) {
            Map<Integer, Integer> convertedProgress = convertBitmapToReadMap(progress.getProgressData());
            // 비트맵 -> 해시맵 변환
            log.debug("✅ Bitmap -> HashMap 변환 완료 : {}", progress.getBible().getKorName());
            allProgress.put(progress.getBible().getId(), convertedProgress);
        }

        return allProgress;
    }

    // 해당 '장'을 기록 + 하루 읽은 양 체크
    @Transactional
    public void recordProgress(Long userId, RecordRequest request) {

        Bible bible = bibleRepository.findById(request.bibleId()).orElseThrow(() -> new EntityNotFoundException("해당 성경이 존재하지 않습니다."));
        // ---> 어차피 bible은 totalChapter를 알아야하기 때문에 getReferenceById써도 의미가 없음
        Progress progress = progressRepository.findByUserIdAndBibleId(userId, request.bibleId())
                .orElseGet(() -> {
                    // 읽은 기록이 없는 경우 새롭게 생성
                    User user = userRepository.getReferenceById(userId); // <<<< getReferenceById는 조회 쿼리 안날림!
                    Progress newProgress = new Progress(user, bible);
                    return progressRepository.save(newProgress);
                    // 새로 생성한건 save를 명시적으로 해줘야 영속 상태가 된다
                });


        Map<Integer, Integer> convertedProgress = convertBitmapToReadMap(progress.getProgressData());

        convertedProgress.merge(request.chapterNumber(), 1, Integer::sum);
        // ---> 해당 키 값이 없으면 1을 넣고, 있으면 기존 값에 +1을 함

        // 마지막으로 읽은 장 업데이트
        progress.updateLastReadChapter(request.chapterNumber());

        String updatedBitmap = convertReadMapToBitmap(convertedProgress, bible.getTotalChapter());
        log.debug("✅ HashMap -> Bitmap 변환 완료 : {} {}장", bible.getKorName(), request.chapterNumber());

        progress.updateProgressData(updatedBitmap); // progressData 업데이트

        // ----- Daily Progress Update -----

        LocalDate today = LocalDate.now();
        dailyProgressRepository.findByUserIdAndReadDate(userId, today)
                .ifPresentOrElse(
                        DailyProgress::increaseCount,

                        () -> {
                            User user = userRepository.getReferenceById(userId);
                            dailyProgressRepository.save(new DailyProgress(user, today, 1));
                        }

                );
    }

    @Transactional
    public void recordBatchProgress(Long userId, BatchRecordRequest request) {
        if (request.chapterNumbers() == null || request.chapterNumbers().isEmpty()) return;

        Bible bible = bibleRepository.findById(request.bibleId())
                .orElseThrow(() -> new EntityNotFoundException("해당 성경이 존재하지 않습니다."));

        Progress progress = progressRepository.findByUserIdAndBibleId(userId, request.bibleId())
                .orElseGet(() -> {
                    User user = userRepository.getReferenceById(userId);
                    return progressRepository.save(new Progress(user, bible));
                });

        Map<Integer, Integer> convertedProgress = convertBitmapToReadMap(progress.getProgressData());

        int lastChapter = -1;
        for (int chapter : request.chapterNumbers()) {
            convertedProgress.merge(chapter, 1, Integer::sum);
            lastChapter = chapter;
        }

        if (lastChapter != -1) {
            progress.updateLastReadChapter(lastChapter);
        }

        progress.updateProgressData(convertReadMapToBitmap(convertedProgress, bible.getTotalChapter()));

        // ----- Daily Progress Update -----
        LocalDate today = LocalDate.now();
        int count = request.chapterNumbers().size();
        dailyProgressRepository.findByUserIdAndReadDate(userId, today)
                .ifPresentOrElse(
                        dp -> dp.increaseCountBy(count),
                        () -> {
                            User user = userRepository.getReferenceById(userId);
                            dailyProgressRepository.save(new DailyProgress(user, today, count));
                        }
                );
    }

    @Override
    public Map<LocalDate, Integer> getDailyProgress(Long userId) {

        LocalDate oneYearAgo = LocalDate.now()
                .minusYears(1)
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        List<DailyProgressDto> result = dailyProgressRepository.getDailyReadingStats(userId, oneYearAgo);

        Map<LocalDate, Integer> grassMap = result.stream()
                .collect(Collectors.toMap(
                        DailyProgressDto::readDate,
                        DailyProgressDto::totalCount
                ));

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusYears(1);
        Map<LocalDate, Integer> fullGrassData = new LinkedHashMap<>();
        for(LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            fullGrassData.put(date, grassMap.getOrDefault(date, 0));
        }

        return fullGrassData;
    }

    @Transactional
    public void cancelBatchProgress(Long userId, BatchRecordRequest request) {
        if (request.chapterNumbers() == null || request.chapterNumbers().isEmpty()) return;

        Progress progress = progressRepository.findByUserIdAndBibleId(userId, request.bibleId())
                .orElseThrow(() -> new EntityNotFoundException("읽기 기록이 없습니다."));

        Map<Integer, Integer> map = convertBitmapToReadMap(progress.getProgressData());

        for (int chapter : request.chapterNumbers()) {
            int current = map.getOrDefault(chapter, 0);
            if (current <= 1) map.remove(chapter);
            else map.put(chapter, current - 1);
        }

        progress.updateProgressData(convertReadMapToBitmap(map, progress.getBible().getTotalChapter()));
    }

    @Override
    public StatsResponse getStats(Long userId) {
        List<Progress> progressList = progressRepository.findAllByUserId(userId);

        // 구약/신약 전체 장 수
        List<Bible> allBibles = bibleRepository.findAll();
        int oldTotal = allBibles.stream().filter(b -> b.getTestament() == Testament.OLD).mapToInt(Bible::getTotalChapter).sum();
        int newTotal = allBibles.stream().filter(b -> b.getTestament() == Testament.NEW).mapToInt(Bible::getTotalChapter).sum();

        // 읽은 장 수 (한 번 이상 읽은 장만)
        int oldRead = 0, newRead = 0;
        for (Progress p : progressList) {
            int readCount = convertBitmapToReadMap(p.getProgressData()).size();
            if (p.getBible().getTestament() == Testament.OLD) oldRead += readCount;
            else newRead += readCount;
        }

        // 오늘 읽은 장
        LocalDate today = LocalDate.now();
        int todayCount = dailyProgressRepository.findByUserIdAndReadDate(userId, today)
                .map(DailyProgress::getCount)
                .orElse(0);

        // 연속 읽기 스트릭
        Set<LocalDate> readDates = dailyProgressRepository
                .getDailyReadingStats(userId, today.minusYears(1))
                .stream()
                .map(DailyProgressDto::readDate)
                .collect(java.util.stream.Collectors.toSet());

        int streak = 0;
        LocalDate check = readDates.contains(today) ? today : today.minusDays(1);
        while (readDates.contains(check)) {
            streak++;
            check = check.minusDays(1);
        }

        return new StatsResponse(oldRead + newRead, oldTotal + newTotal, oldRead, oldTotal, newRead, newTotal, todayCount, streak);
    }

    // map -> bitmap
    private String convertReadMapToBitmap(Map<Integer, Integer> map, int totalChapter) {
        List<String> list = new ArrayList<>();
        for (int i = 1; i <= totalChapter; i++) {
            list.add(String.valueOf(map.getOrDefault(i, 0)));
        }
        return String.join(",", list);
    }

    // DB에 저장된 비트맵을 Map 형태로 변환해주는 메서드
    private Map<Integer, Integer> convertBitmapToReadMap(String bitmap) {

        /**
         * [1,0,1,3,0, ...]
         */

        Map<Integer, Integer> progress = new HashMap<>(); // <읽은 장, 읽은 횟수>
        String[] splitBitmap = bitmap.split(",");
        for(int i=0; i< splitBitmap.length; i++) {
            if(Integer.parseInt(splitBitmap[i]) == 0) {
                continue;
                // 한번도 안읽었으면 굳이 안넣어도 됨
            }
            progress.put(i+1, Integer.parseInt(splitBitmap[i]));
        }

        return progress;
    }
}
