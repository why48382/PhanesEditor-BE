package org.example.coding_convention.chat.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import org.example.coding_convention.project.model.Project;
import org.example.coding_convention.user.model.User;
import org.example.coding_convention.user.model.UserDto;

public class ChatsDto {

    @Getter
    @Builder
    public static class ChatList {
        private Integer idx;
        private String username;
        private String message;
        private String sentAt;

        public static ChatsDto.ChatList from(Chats chatsEntity) {
            return ChatList.builder()
                    .idx(chatsEntity.getIdx())
                    .username(chatsEntity.getUser().getNickname())
                    .message(chatsEntity.getMessage())
                    .sentAt(chatsEntity.getSentAt().toString())
                    .build();
        }
    }

    @Getter
    @Schema(description = "채팅 저장")
    public static class ChatRegister {
        @Schema(description = "채팅을 작성중인 프로젝트의 id", example = "1")
        private Integer projectId;
        @Schema(description = "채팅을 작성한 유저의 id", example = "입력 x")
        private Integer userId;
        @NotBlank(message = "채팅 내용은 공백을 허용하지 않습니다.")
        @Schema(description = "채팅 내용", example = "안녕하세요")
        private String message;

        public Chats toEntity(UserDto.AuthUser authUser) {
            Project projectEntity = Project.builder()
                    .idx(projectId)
                    .build();
            User userEntity = User.builder()
                    .idx(authUser.getIdx())
                    .build();
            return Chats.builder()
                    .project(projectEntity)
                    .user(userEntity)
                    .message(message)
                    .build();
        }
    }


}
