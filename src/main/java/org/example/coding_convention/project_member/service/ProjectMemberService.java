package org.example.coding_convention.project_member.service;

import lombok.RequiredArgsConstructor;
import org.example.coding_convention.project_member.model.ProjectMember;
import org.example.coding_convention.project_member.model.ProjectMemberDto;
import org.example.coding_convention.project_member.repository.ProjectMemberRepository;
import org.example.coding_convention.user.model.UserDto;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {
    private final ProjectMemberRepository projectMemberRepository;

    public void newMember(ProjectMemberDto.ProjectMemberReq dto, UserDto.AuthUser authUser) {
        ProjectMember requester = projectMemberRepository.findByProject_IdxAndUser_Idx(dto.getProjectId(), authUser.getIdx()).orElseThrow();
        if (requester.getStatus() != ProjectMember.Status.ADMIN) {
            throw new RuntimeException("어드민만 변경할 수 있습니다.");
        }
        if (Objects.equals(dto.getUserId(), authUser.getIdx())) {
            throw new RuntimeException("자기 자신을 추가할 수 없습니다.");
        }
        projectMemberRepository.save(dto.toEntity(dto.getStatus(), dto.getUserId()));
    }

    public void delete(ProjectMemberDto.ProjectMemberReq dto, UserDto.AuthUser authUser) {
        ProjectMember requester = projectMemberRepository.findByProject_IdxAndUser_Idx(dto.getProjectId(), authUser.getIdx()).orElseThrow();
        if (requester.getStatus() != ProjectMember.Status.ADMIN) {
            throw new RuntimeException("어드민만 변경할 수 있습니다.");
        }
        if (Objects.equals(dto.getUserId(), authUser.getIdx())) {
            throw new RuntimeException("자기 자신을 삭제할 수 없습니다.");
        }

        ProjectMember target = projectMemberRepository.findByProject_IdxAndUser_Idx(dto.getProjectId(), dto.getUserId())
                .orElseThrow(() -> new RuntimeException("해당 멤버를 찾을 수 없습니다."));

        projectMemberRepository.delete(target);
    }
}
