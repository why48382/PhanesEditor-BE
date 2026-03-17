package org.example.coding_convention.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.coding_convention.chat.model.Chats;
import org.example.coding_convention.likes.model.Like;
import org.example.coding_convention.project.model.Project;
import org.example.coding_convention.project_member.model.ProjectMember;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(
        name = "users", // 일부 DB에서 user 는 예약어라 충돌 방지
        indexes = {
                @Index(name = "idx_users_platform_key", columnList = "platform, platformKey"),
                @Index(name = "idx_users_email", columnList = "email"),
                @Index(name = "idx_users_nickname", columnList = "nickname")
        }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 용 기본 생성자
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) // @CreatedDate, @LastModifiedDate 동작

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx;

    @Column(nullable = false, length = 120, unique = true)
    private String email;

    @Column(nullable = false, length = 200)
    private String nickname;

    @Column(length = 100)
    private String password;

    @Column(name = "profile_img", length = 500)
    private String profileImg;

    // 닉네임 변경 시각을 기록
    private LocalDateTime nickUpdatedAt;

    // 생성/수정 시간은 감사로 자동 세팅됨
    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 로그인 플랫폼 (숫자 Integer 대신 Enum 권장)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Platform platform;

    @Column(length = 200)
    private String platformKey; // 소셜 플랫폼의 유일키(식별자)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(length = 100)
    private String browser;

    private Boolean enabled;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private List<Project> projectList;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private List<ProjectMember> projectMemberList;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    List<EmailVerify> emailVerifyList;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private List<Chats> chatsList;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    private List<Like> likeList;

    public void userVerify() {this.enabled=true;}

    // ===== 내부 Enum =====
    public enum Status {
        ACTIVE,    // 활성화
        INACTIVE,  // 비활성화
        BANNED     // 정지
    }

    public enum Platform {
        LOCAL, KAKAO, GOOGLE
    }

    // ===== 도메인 메서드 =====
    public void updatePassword(String passwordEncoding) {
        this.password = passwordEncoding;
    }

    /** 로그인 성공 시 호출됨 */
    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }

    /** 닉네임 변경 시각 갱신 */
    public void touchNickUpdatedAt() {
        this.nickUpdatedAt = LocalDateTime.now();
    }

    /** 상태 변경 (비즈니스 규칙 점검 후 호출) */
    public void changeStatus(Status next) {
        this.status = next;
    }

    /** 최초 저장 직전에 기본값 세팅 */
    @PrePersist
    private void prePersist() {
        if (this.status == null) this.status = Status.ACTIVE;
        if (this.platform == null) this.platform = Platform.LOCAL;
    }

}
