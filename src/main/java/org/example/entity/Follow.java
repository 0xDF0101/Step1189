package org.example.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class) // --> createAt에 시간 넣어줌
@Getter
@NoArgsConstructor
@Table(name = "follow")
public class Follow {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id") // --> 실제로는 객체 자체가 아니라 pk만 저장된다!
    private User follower; // 팔로우를 한 사람 (나)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id")
    private User following; // 팔로우를 받은 사람

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder
    public Follow(User follower, User following) {
        this.follower = follower;
        this.following = following;
    }



}
