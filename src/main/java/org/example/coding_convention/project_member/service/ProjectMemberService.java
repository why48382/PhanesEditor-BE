package org.example.coding_convention.project_member.service;

import lombok.RequiredArgsConstructor;
import org.example.coding_convention.project_member.model.ProjectMemberDto;
import org.example.coding_convention.project_member.repository.ProjectMemberRepository;
import org.example.coding_convention.user.model.User;
import org.example.coding_convention.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProjectMemberService {
    private final ProjectMemberRepository projectRepository;
    private final UserService userService;

    public void save(ProjectMemberDto.ProjectMemberReq dto) {
        User user = userService.findByNickName(dto.getNickname()).orElseThrow();
        String status = dto.getStatus();
        projectRepository.save(dto.toEntity(status, user.getIdx()));
    }
}
