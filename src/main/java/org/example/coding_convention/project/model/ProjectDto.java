package org.example.coding_convention.project.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.example.coding_convention.chat.model.Chats;
import org.example.coding_convention.chat.model.ChatsDto;
import org.example.coding_convention.file.model.Files;
import org.example.coding_convention.file.model.FilesDto;
import org.example.coding_convention.project_member.model.ProjectMember;
import org.example.coding_convention.project_member.model.ProjectMemberDto;
import org.example.coding_convention.user.model.User;
import org.example.coding_convention.user.model.UserDto;

import java.util.List;

public class ProjectDto {

    @Getter
    @Schema(description = "프로젝트 저장용 DTO")
    @Builder
    public static class ProjectReq {
        @Schema(description = "프로젝트 이름", example = "자바 프로젝트")
        private String projectName;
        @Schema(description = "프로젝트 설명", example = "자바로 알고리즘을 구현해 보자")
        private String description;
        @Schema(description = "프로젝트 대표 사용 언어", example = "JAVA")
        private String language;
        @Schema(description = "프로젝트를 생성한 사용자의 idx", example = "입력 x")
        private Integer userId;
        @Schema(description = "프로젝트에 초대할 사용자의 idx", example = "[1, 2, 3]")
        private List<Integer> memberIdx;

        public Project toEntity(String url, User user) {
            Project.Language dtoLanguage = Project.projectLanguage(language);
            return Project.builder()
                    .projectName(projectName)
                    .description(description)
                    .url(url)
                    .language(dtoLanguage)
                    .user(user)
                    .build();
        }
    }


    @Getter
    @Builder
    public static class ProjectRead {
        private Integer idx;
        private String projectName;
        private String language;
        private Integer userIdx;
        private List<FilesDto.FilesList> projectFile;
        private List<ProjectMemberDto.ProjectMemberList> projectMember;
        private List<ChatsDto.ChatList> projectChat;

        public static ProjectRead from(Project entity, UserDto.AuthUser authUser) {
            List<Files> filesList = entity.getFileList();
            List<ProjectMember> projectMemberList = entity.getProjectMemberList();
            List<Chats> projectChatList = entity.getChatsList();

            return ProjectRead.builder()
                    .idx(entity.getIdx())
                    .projectName(entity.getProjectName())
                    .language(entity.getLanguage().toString())
                    .userIdx(authUser.getIdx())
                    .projectMember(projectMemberList.stream().map(ProjectMemberDto.ProjectMemberList::from).toList())
                    .projectFile(filesList.stream().map(FilesDto.FilesList::from).toList())
                    .projectChat(projectChatList.stream().map(ChatsDto.ChatList::from).toList())
                    .build();
        }

        public static ProjectRead from(Project entity) {
            List<Files> filesList = entity.getFileList();
            List<ProjectMember> projectMemberList = entity.getProjectMemberList();
            List<Chats> projectChatList = entity.getChatsList();

            return ProjectRead.builder()
                    .idx(entity.getIdx())
                    .projectName(entity.getProjectName())
                    .language(entity.getLanguage().toString())
                    .projectMember(projectMemberList.stream().map(ProjectMemberDto.ProjectMemberList::from).toList())
                    .projectFile(filesList.stream().map(FilesDto.FilesList::from).toList())
                    .projectChat(projectChatList.stream().map(ChatsDto.ChatList::from).toList())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ProjectList {
        private Integer idx;
        private String projectName;
        private String description;
        private String language;
        private String creator;
        private boolean isOwner;

        public static ProjectList from(ProjectMember entity, Integer myIdx) {
            boolean isOwner = entity.getProject().getUser().getIdx().equals(myIdx);

            return ProjectList.builder()
                    .idx(entity.getProject().getIdx())
                    .projectName(entity.getProject().getProjectName())
                    .description(entity.getProject().getDescription())
                    .language(entity.getProject().getLanguage().toString())
                    .creator(entity.getProject().getUser().getNickname())
                    .isOwner(isOwner)
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ProjectSearchRes {
        private Integer idx;
        private String name;
        private String creator;
        private String language;
        private String description;
        private Integer likeCount;

        public static ProjectDto.ProjectSearchRes from(Project entity) {

            ProjectSearchRes dto = ProjectSearchRes.builder()
                    .idx(entity.getIdx())
                    .name(entity.getProjectName())
                    .creator(entity.getUser().getNickname())
                    .language(entity.getLanguage().toString())
                    .description(entity.getDescription())
                    .likeCount(entity.getLikeCount())
                    .build();

            return dto;
        }
    }

    @Getter
    @Builder
    public static class ProjectRes {
        private Integer idx;

        public static ProjectDto.ProjectRes from(Project entity) {
            return ProjectRes.builder()
                    .idx(entity.getIdx())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class ProjectAndMember {
        private String projectName;
        private String description;
        private String language;
        private Integer userId;
        private ProjectMember projectMember;

        public ProjectDto.ProjectAndMember from(Project entity, ProjectMember member) {
            ProjectMember projectMember1 = ProjectMember.builder()
                    .idx(member.getIdx())
                    .status(member.getStatus())
                    .user(member.getUser())
                    .project(member.getProject())
                    .build();
            return ProjectAndMember.builder()
                    .projectName(entity.getProjectName())
                    .description(entity.getDescription())
                    .language(entity.getLanguage().toString())
                    .userId(entity.getIdx())
                    .projectMember(projectMember1)
                    .build();
        }
    }

}
