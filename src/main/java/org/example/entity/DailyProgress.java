package org.example.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;

@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "readDate"})
        // 한 유저는 날짜당 하나의 Row만 생성되도록 '복합 유니크 제약 조건'을 설정한 것
})
@Getter
@NoArgsConstructor
public class DailyProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private int count;

    @CreatedDate
    private LocalDate readDate;


    public DailyProgress(User user, LocalDate readDate, int count) {
        this.user = user;
        this.readDate = readDate;
        this.count = count;
    }

    public void increaseCount() {
        count++;
    }

    public void increaseCountBy(int amount) {
        count += amount;
    }
}
