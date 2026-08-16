package org.example.coding_convention.config.interceptor;

import lombok.RequiredArgsConstructor;
import org.example.coding_convention.file.repository.FileRepository;
import org.example.coding_convention.project_member.repository.ProjectMemberRepository;
import org.example.coding_convention.user.model.UserDto;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {
    private final ProjectMemberRepository projectMemberRepository;
    private final FileRepository fileRepository;
    private static final Set<String> ROOM_PREFIXES = Set.of(
            "/topic/editor/", "/topic/chat/", "/app/editor/", "/app/chat/", "/topic/project/"
    );

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Map<String, Object> attributes = accessor.getSessionAttributes();
            if (attributes != null) {
                Authentication authentication = (Authentication) attributes.get("auth");
                if (authentication != null) {
                    accessor.setUser(authentication);
                }
            }
            return message;
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand()) || StompCommand.SEND.equals(accessor.getCommand())) {
            Integer roomId = extractRoomId(accessor.getDestination());
            if (roomId == null) {
                return message; // room 관련 목적지가 아니면 그냥 통과
            }

            Object userPrincipal = accessor.getUser();
            if (!(userPrincipal instanceof Authentication authentication)
                    || !(authentication.getPrincipal() instanceof UserDto.AuthUser authUser)) {
                return null; // 인증 안 된 세션 -> 차단
            }

            Integer projectIdx = resolveProjectIdx(accessor.getDestination(), roomId);
            if (projectIdx == null) {
                return null;
            }

            boolean isMember = projectMemberRepository.existsByProject_IdxAndUser_Idx(projectIdx, authUser.getIdx());
            if (!isMember) {
                return null; // 해당 프로젝트 멤버 아님 -> 차단
            }
        }

        return message;
    }

    private Integer extractRoomId(String destination) {
        if (destination == null) return null;
        for (String prefix : ROOM_PREFIXES) {
            if (destination.startsWith(prefix)) {
                String rest = destination.substring(prefix.length());
                int slashIdx = rest.indexOf('/');
                String idPart = slashIdx >= 0 ? rest.substring(0, slashIdx) : rest;
                try {
                    return Integer.parseInt(idPart);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private Integer resolveProjectIdx(String destination, Integer roomId) {
        if (destination.startsWith("/topic/editor/") || destination.startsWith("/app/editor/")) {
            return fileRepository.findProjectIdxByFileIdx(roomId).orElse(null);
        }
        return roomId;
    }
}