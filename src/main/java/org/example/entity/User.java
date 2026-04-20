package org.example.entity;

import jakarta.persistence.*;
import lombok.*;
import org.example.model.Role;
import org.example.dto.user.UserCreateRequest;
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
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    // cascade가 all일시, user(부모)가 삭제되면 progress(자식)의 테이블도 함께 삭제됨!
    private List<Progress> progressList = new ArrayList<>();

    @Column(length = 100)
    private String password;

    // 어떤 방식으로 로그인했는지
    // Local, Google, Naver, Apple 등
    @Column(name = "social_type")
    private String socialType;

    @Column(name = "username",  length = 50, nullable = false, unique = true)
    private String username;

//    private String name;
    // ---> 실명은 굳이 입력할 이유가 없다!

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

    // OAuth 제공자 ID
    private String providerId;

    // OAuth 가입했을 시 따로 username 입력받은 경우 써먹음
    public void updateUsernameAndRole(String username, Role role) {
        this.username = username;
        this.role = role;
    }
}
