package org.example.coding_convention.project_member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.coding_convention.common.response.BaseResponse;
import org.example.coding_convention.project_member.model.ProjectMemberDto;
import org.example.coding_convention.project_member.service.ProjectMemberService;
import org.example.coding_convention.user.model.UserDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "프로젝트 참가 맴버")
@RestController
@RequestMapping("/project/member")
@RequiredArgsConstructor
public class ProjectMemberController {
    private final ProjectMemberService projectService;

    @Operation(
            summary = "프로젝트 맴버 추가",
            description = "프로젝트에 참여할 유저를 추가합니다."
    )

    @PostMapping("/addmember")
    public BaseResponse<String> projectMemberUpdate(@RequestBody ProjectMemberDto.ProjectMemberReq dto, @AuthenticationPrincipal UserDto.AuthUser authUser) {
        projectService.newMember(dto, authUser);
        return BaseResponse.success("맴버 업데이트 성공");
    }

    @DeleteMapping("/removemember")
    public BaseResponse<String> projectMemberDelete(@RequestBody ProjectMemberDto.ProjectMemberReq dto, @AuthenticationPrincipal UserDto.AuthUser authUser) {
        projectService.delete(dto, authUser);
        return BaseResponse.success("맴버 제거 성공");
    }

}
