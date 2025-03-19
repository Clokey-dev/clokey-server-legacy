package com.clokey.server.domain.member.domain.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.*;

import lombok.*;

import com.clokey.server.domain.cloth.domain.entity.Cloth;
import com.clokey.server.domain.history.domain.entity.History;
import com.clokey.server.domain.history.domain.entity.MemberLike;
import com.clokey.server.domain.member.dto.MemberDTO;
import com.clokey.server.domain.model.entity.BaseEntity;
import com.clokey.server.domain.model.entity.enums.MemberStatus;
import com.clokey.server.domain.model.entity.enums.RegisterStatus;
import com.clokey.server.domain.model.entity.enums.SocialType;
import com.clokey.server.domain.model.entity.enums.Visibility;
import com.clokey.server.domain.term.domain.entity.MemberTerm;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@Setter
@DynamicUpdate
@DynamicInsert
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(length = 30)
    private String nickname;

    @Column(unique = true)
    private String clokeyId;

    @Column(length = 100) //한줄 소개
    private String bio;

    @Enumerated(EnumType.STRING) //가입종류
    @Column(nullable = false)
    private SocialType socialType;

    private String profileImageUrl;

    private String profileBackImageUrl;

    @Enumerated(EnumType.STRING) //활성화여부
    @Column(columnDefinition = "VARCHAR(15) DEFAULT 'ACTIVE'", nullable = false)
    private MemberStatus status;

    @Enumerated(EnumType.STRING) //성별
    @Column(columnDefinition = "VARCHAR(30) DEFAULT 'NOT_AGREED'", nullable = false)
    private RegisterStatus registerStatus;

    private LocalDate inactiveDate;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "VARCHAR(15) DEFAULT 'PUBLIC'", nullable = false) // 공개 범위
    private Visibility visibility;

    @Column(nullable = true, unique = true)
    private String refreshToken;

    @Column(nullable = true, unique = true)
    private String accessToken;

    @Column(nullable = true, unique = false)
    private String deviceToken;

    @Column(nullable = true, unique = true)
    private String appleRefreshToken;

    @Column(unique = true)
    private String kakaoId;

    //default가 false입니다.
    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private boolean banned;

    //필요한 양방향 매핑을 제외하고 삭제해주세요.
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberTerm> memberTermList = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<MemberLike> memberLikeList = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Cloth> clothList = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<History> historyList = new ArrayList<>();


    public void profileUpdate(MemberDTO.ProfileRQ request, String profileImageUrl, String profileBackImageUrl) {
        this.nickname = request.getNickname();
        this.clokeyId = request.getClokeyId();
        this.profileImageUrl = profileImageUrl;
        this.bio = request.getBio();
        this.profileBackImageUrl = profileBackImageUrl;
        this.visibility = request.getVisibility();
    }

    public void deleteAccessRefreshToken() {
        this.refreshToken = null;
        this.accessToken = null;
    }

    public void updateRegisterStatus(RegisterStatus registerStatus) {
        this.registerStatus = registerStatus;
    }

    public void updateToken(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public void updateAppleRefreshToken(String appleRefreshToken) {
        this.appleRefreshToken = appleRefreshToken;
    }

    public void updateKakaoId(String kakaoId) {
        this.kakaoId = kakaoId;
    }

    public void updateDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public void updateStatus() {
        if (this.status == MemberStatus.ACTIVE) {
            this.status = MemberStatus.INACTIVE;
        } else {
            this.status = MemberStatus.ACTIVE;
        }
    }

    public void updateInactiveDate(LocalDate date) {
        this.inactiveDate = date;
    }


}
