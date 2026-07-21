package org.example.coding_convention.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.coding_convention.common.response.BaseResponse;
import org.example.coding_convention.user.model.UserDto;
import org.example.coding_convention.user.service.UserService;
import org.example.coding_convention.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Tag(name = "User", description = "회원 관련 API")
public class UserController {
    @Value("${cookie.secure}")
    private boolean cookieSecure;
    @Value("${cookie.same-site}")
    private String cookieSameSite;
    @Value("${cookie.domain}")
    private String cookieDomain;
    private final UserService userService;

    @Operation(
            summary = "회원가입",
            description = "새로운 회원을 등록하고 인증 메일을 전송합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BaseResponse.class),
                            examples = @ExampleObject(
                                    name = "회원가입 성공 예시",
                                    value = """
                                            {
                                              "success": true,
                                              "message": "요청에 성공했습니다.",
                                              "results": "이메일 인증요망"
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/signup")
    public BaseResponse signup(@RequestBody UserDto.Register dto) throws MessagingException {
        userService.signup(dto);

        return BaseResponse.success("이메일 인증요망");
    }

    @Operation(
            summary = "프로필 조회",
            description = "로그인한 사용자의 프로필 정보를 조회합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "프로필 조회 성공",
                    content = @Content(schema = @Schema(implementation = UserDto.Profile.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @GetMapping("/usr_mypage")
    public BaseResponse usrMypage(@AuthenticationPrincipal UserDto.AuthUser authUser) {
        UserDto.Profile dto = userService.findProfile(authUser);

        return BaseResponse.success(dto);
    }

    @Operation(
            summary = "프로필 이미지 업로드",
            description = "사용자의 프로필 이미지를 업로드/변경합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @PostMapping("/usr_mypage/image")
    public BaseResponse uploadProfileImage(
            @RequestParam("profileImage") MultipartFile file,
            @AuthenticationPrincipal UserDto.AuthUser authUser) {

        String imagePath = userService.updateImage(file, authUser);
        return BaseResponse.success(imagePath);
    }

    @Operation(
            summary = "이메일 인증기능",
            description = "이메일 인증시 실행되는 기능"
    )
    @GetMapping("/verify")
    public BaseResponse verify(String uuid) {
        userService.verify(uuid);

        return BaseResponse.success("이메일 인증완료");
    }

    @Operation(
            summary = "로그아웃",
            description = "클라이언트의 토큰을 제거"
    )
    @PostMapping("/logout")
    public BaseResponse<String> logout(HttpServletResponse response) {
        JwtUtil.deleteToken(response, cookieSecure, cookieSameSite, cookieDomain);
        return BaseResponse.success("로그아웃 완료");
    }

    @Operation(
            summary = "유저 검색",
            description = "닉네임으로 유저를 검색"
    )
    @GetMapping("/search")
    public BaseResponse<List<UserDto.UserSearch>> userSearch(@RequestParam String nickname) {
        List<UserDto.UserSearch> result = userService.userSearch(nickname);
        return BaseResponse.success(result);
    }
}
