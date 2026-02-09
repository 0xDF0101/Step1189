package org.example.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.model.Role;
import org.example.user.dto.UserCreateRequest;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    // cascade가 all일시, user(부모)가 삭제되면 progress(자식)의 테이블도 함께 삭제됨!
    private List<Progress> progressList = new ArrayList<>();


    @Column(name = "nickname",  length = 50, nullable = false, unique = true)
    private String nickname;

    @Column(unique = true)
    private String email;

    // 권한
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // 공개 여부
    @Column(name = "is_public")
    private Boolean publicStatus = true;

    // 상태 메시지 같은거
    private String statusMessage;

    // 프로필 이미지 링크
    private String profileImgUrl;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // OAuth 제공자
    @Column(nullable = false)
    private String provider;

    // OAuth 제공자 ID
    @Column(nullable = false)
    private String providerId;

    public User(UserCreateRequest request) {
        this.email = request.email();
        this.nickname = request.nickname();
        this.statusMessage = request.statusMessage();
        this.provider = "Google";
        this.providerId = "000000";
        this.role = Role.USER;
    }

}
