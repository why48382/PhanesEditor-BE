package org.example.coding_convention.user.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class UserDto {

    @Getter
    @Schema(name = "User.Register", description = "회원가입 기능")
    public static class Register {
        @Schema(description = "이메일 작성", example = "test01@test.com")
        @Pattern(message = "이메일 형식을 사용해주세요", regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
        private String email;
        @Pattern(message = "비밀번호는 숫자,영문 대소문자,특수문자( !@#$%^&*() )를 조합해 8~20자로 생성해주세요.", regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[!@#$%^&*()])(?=.*[0-9]).{8,20}$")
        @Schema(description = "비밀번호 작성", example = "Qwer1234!")
        private String password;
        @Pattern(message = "이름은 한글만 가능합니다", regexp = "^[가-힣]{1,50}$")
        @Schema(description = "사용자 이름 작성", example = "홍길동")
        private String nickname;

        public User toEntity() {
            User entity = User.builder()
                    .email(email)
                    .password(password)
                    .nickname(nickname)
                    .enabled(false)
                    .build();

            return entity;
        }
    }

    @Builder
    @Getter
    public static class AuthUser implements UserDetails, OAuth2User {
        private Integer idx;
        private String email;
        private String password;
        private String nickname;
        private Boolean enabled;
        private Map<String, Object> attributes;

        @Override
        public String getName() {
            return (nickname != null && !nickname.isBlank()) ? nickname : email;
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return List.of(new SimpleGrantedAuthority("ROLE_USER"));
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getUsername() {
            return email;
        }

        public static AuthUser from(User entity) {
            AuthUser dto = AuthUser.builder()
                    .idx(entity.getIdx())
                    .email(entity.getEmail())
                    .password(entity.getPassword())
                    .nickname(entity.getNickname())
                    .enabled(entity.getEnabled())
                    .build();

            return dto;
        }
    }

    @Getter
    public static class Login {
        private String email;
        private String password;
    }

    @Getter
    @Builder
    @Schema(description = "프로필 응답 DTO")
    public static class LoginRes {
        @Schema(description = "회원 고유 ID", example = "1")
        private Integer idx;
        @Schema(description = "회원 이메일", example = "admin@example.com")
        private String email;
        @Schema(description = "회원 닉네임", example = "관리자")
        private String nickname;

        public static LoginRes from(AuthUser authUser) {
            LoginRes dto = LoginRes.builder()
                    .idx(authUser.getIdx())
                    .email(authUser.getEmail())
                    .nickname(authUser.getNickname())
                    .build();

            return dto;
        }
    }

    @Getter
    @Builder
    public static class Profile {
        private String nickName;
        private LocalDateTime createdAt;
        private String email;
        private String plafForm;
        private String imgUrl;

        public static Profile from(User entity) {
            Profile dto = Profile.builder()
                    .nickName(entity.getNickname())
                    .createdAt(entity.getCreatedAt())
                    .email(entity.getEmail())
                    .plafForm(entity.getPlatform().toString())
                    .imgUrl(entity.getProfileImg())
                    .build();

            return dto;
        }
    }

    @Getter
    @Builder
    public static class UserSearch {
        private Integer idx;
        private String nickname;

        public static UserSearch from(User entity) {
            return UserSearch.builder()
                    .idx(entity.getIdx())
                    .nickname(entity.getNickname())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class UpdateProfile {
        private String nickname;
        private String profileImg;

        public static UpdateProfile from(User entity) {
            return UpdateProfile.builder()
                    .nickname(entity.getNickname())
                    .profileImg(entity.getProfileImg())
                    .build();
        }
    }
}




