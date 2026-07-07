package org.example.coding_convention.project.service;

import lombok.RequiredArgsConstructor;
import org.example.coding_convention.project.model.Project;
import org.example.coding_convention.project.model.ProjectDto;
import org.example.coding_convention.project.repository.ProjectQueryRepository;
import org.example.coding_convention.project.repository.ProjectRepository;
import org.example.coding_convention.project_member.model.ProjectMember;
import org.example.coding_convention.project_member.model.ProjectMemberDto;
import org.example.coding_convention.project_member.repository.ProjectMemberRepository;
import org.example.coding_convention.user.model.User;
import org.example.coding_convention.user.model.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectQueryRepository projectQueryRepository;


    public Page<ProjectDto.ProjectSearchRes> search(String projectName, String email, String language, Pageable pageable) {

        Page<Project> result = projectQueryRepository.searchProjects(projectName, email, language, pageable);
        return result.map(ProjectDto.ProjectSearchRes::from);
        // result.stream().map(ProjectDto.ProjectSearchRes::from).toList();
    }

    public ProjectDto.ProjectRes save(ProjectDto.ProjectReq dto, UserDto.AuthUser authUser) {
        User userIdx = User.builder()
                .idx(authUser.getIdx())
                .build();

        String url = "test";

        Project project = projectRepository.save(dto.toEntity(url, userIdx));

        Project projectIdx = Project.builder() // TODO 이거 제거하기
                .idx(project.getIdx())
                .build();

        String status = "ADMIN";

        ProjectMemberDto.ProjectMemberReq memberDto = ProjectMemberDto.ProjectMemberReq.builder()
                .projectId(projectIdx.getIdx()) // 여기
                .userId(userIdx.getIdx())
                .status(status)
                .build();

        projectMemberRepository.save(memberDto.toEntity());
        // 저장이 진짜 되었는지 검증하고 싶으면 엔티티 반환해주면 됨
        for (Integer memIdx : dto.getMemberIdx()) {
            ProjectMemberDto.ProjectMemberReq projectMembers= ProjectMemberDto.ProjectMemberReq.builder()
                    .projectId(project.getIdx()) // 여기
                    .userId(memIdx)
                    .status("USER")
                    .build();
            projectMemberRepository.save(projectMembers.toEntity());
        }
        return ProjectDto.ProjectRes.from(project);
    }

    // TODO 자기자신을 초대해서 프로젝트를 만들면 안됨
    public ProjectDto.ProjectRead read(Integer idx, UserDto.AuthUser authUser) {
        Optional<Project> result = projectRepository.findByProjectIdx(idx);
        if (result.isPresent()) {
            Project project = result.get();
            if (authUser == null) {
                return ProjectDto.ProjectRead.from(project);
            }
            return ProjectDto.ProjectRead.from(project, authUser);
        }
        return null;
    }

    public List<ProjectDto.ProjectList> list(UserDto.AuthUser authUser) {
        Integer userId = authUser.getIdx();
        List<ProjectMember> result = projectMemberRepository.findByProjectList(userId);
        return result.stream().map(ProjectDto.ProjectList::from).toList();
    }
}
