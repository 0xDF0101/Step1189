package org.example.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "group_goals")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false, unique = true)
    private Group group;

    @Column(nullable = false)
    private int targetChaptersPerPerson;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public GroupGoal(Group group, int targetChaptersPerPerson, LocalDate startDate, LocalDate endDate) {
        this.group = group;
        this.targetChaptersPerPerson = targetChaptersPerPerson;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void update(int targetChaptersPerPerson, LocalDate startDate, LocalDate endDate) {
        this.targetChaptersPerPerson = targetChaptersPerPerson;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
