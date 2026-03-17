package org.example.coding_convention.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.coding_convention.common.response.BaseResponse;
import org.example.coding_convention.project.model.ProjectDto;
import org.example.coding_convention.project.service.ProjectService;
import org.example.coding_convention.user.model.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "프로젝트")
@RestController
@RequestMapping("/project")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    @Operation(
            summary = "프로젝트 생성",
            description = "프로젝트의 이름, 설명 등을 담은 프로젝트가 생성됩니다."
    )
    @PostMapping("/create")
    public BaseResponse<ProjectDto.ProjectRes> projectCreate(@RequestBody ProjectDto.ProjectReq dto, @AuthenticationPrincipal UserDto.AuthUser authUser) {
        ProjectDto.ProjectRes result = projectService.save(dto, authUser);
        return BaseResponse.success(result);
    }

    @Operation(
            summary = "프로젝트 목록 조회",
            description = "프로젝트의 내가 참여중인 프로젝트의 목록을 조회합니다."
    )
    @GetMapping("/list")
    public BaseResponse<List<ProjectDto.ProjectList>> projectList(@AuthenticationPrincipal UserDto.AuthUser authUser) {
        List<ProjectDto.ProjectList> result = projectService.list(authUser);
        return BaseResponse.success(result);
    }

    @Operation(
            summary = "프로젝트 상세 조회",
            description = "내가 고른 프로젝트의 파일, 맴버, 채팅을 조회합니다."
    )
    @GetMapping("/read")
    public BaseResponse<ProjectDto.ProjectRead> projectRead(@RequestParam Integer idx, @AuthenticationPrincipal UserDto.AuthUser authUser) {
        ProjectDto.ProjectRead result = projectService.read(idx, authUser);
        return BaseResponse.success(result);
    }

    @GetMapping("/search")
    public BaseResponse<Page<ProjectDto.ProjectSearchRes>> projectSearch(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String language,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        if (name.isEmpty() && email.isEmpty() && language.isEmpty()) {
            throw new IllegalArgumentException("최소 하나 이상의 검색 조건을 입력해야 합니다.");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("idx").descending());

        Page<ProjectDto.ProjectSearchRes> result = projectService.search(name, email, language, pageable);
        return BaseResponse.success(result);
    }


}
