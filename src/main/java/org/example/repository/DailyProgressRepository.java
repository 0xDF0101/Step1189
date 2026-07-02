package org.example.repository;

import org.example.dto.progress.DailyProgressDto;
import org.example.entity.DailyProgress;
import org.example.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyProgressRepository extends JpaRepository<DailyProgress, Long> {

    Optional<DailyProgress> findByUserIdAndReadDate(Long userId, LocalDate readDate);


    @Query("SELECT new org.example.dto.progress.DailyProgressDto(dp.readDate, CAST(SUM(dp.count) AS int)) " +
            "FROM DailyProgress dp " +
            "WHERE dp.user.id = :userId AND dp.readDate >= :oneYearAgo " +
            "GROUP BY dp.readDate")
    List<DailyProgressDto> getDailyReadingStats(@Param("userId") Long userId, @Param("oneYearAgo") LocalDate oneYearAgo);

    @Query("SELECT COALESCE(SUM(dp.count), 0) FROM DailyProgress dp " +
            "WHERE dp.user.id = :userId AND dp.readDate BETWEEN :start AND :end")
    int sumChaptersInPeriod(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    void deleteByUser(User user);

    @Query("SELECT COALESCE(SUM(dp.count), 0) FROM DailyProgress dp WHERE dp.user.id = :userId")
    int sumAllChaptersByUserId(@Param("userId") Long userId);

}
