package org.example.entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.model.Testament;

import java.util.ArrayList;
import java.util.List;

/**
 * 성경의 '권'에 해당하는 테이블
 */

@Entity
@Table(name = "bibles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bible {

    @Id
    @Column(nullable = false, updatable = false)
    private int id;

//    @OneToMany(mappedBy = "bible")
//    private List<Progress> progressList = new ArrayList<>();
    // bible은 사실상 참조를 '당하기'만 하기 때문에 굳이 양방향을 유지할 필요가 없음



    @Column(nullable = false, updatable = false)
    private String korName;

    @Column(nullable = false, updatable = false)
    private String engName;

    // 장수
    @Column(nullable = false, updatable = false)
    private int totalChapter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false)
    private Testament testament;

    public Bible(Integer id, String korName, String engName, int totalChapter, Testament testament) {
        this.id = id;
        this.korName = korName;
        this.engName = engName;
        this.totalChapter = totalChapter;
        this.testament = testament;
    }
}
