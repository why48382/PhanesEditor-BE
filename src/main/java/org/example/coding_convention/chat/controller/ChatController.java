package org.example.coding_convention.chat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.coding_convention.chat.model.ChatsDto;
import org.example.coding_convention.chat.service.ChatService;
import org.example.coding_convention.common.response.BaseResponse;
import org.example.coding_convention.user.model.UserDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "채팅", description = "프로젝트 채팅기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;

    @Operation(
            summary = "채팅 저장",
            description = "채팅 저장 기능 누가 어떤 채팅을 작성했는지 저장됩니다."
    )
    @PostMapping("/register")
    public BaseResponse<String> chatRegister(@Valid @RequestBody ChatsDto.ChatRegister dto, @AuthenticationPrincipal UserDto.AuthUser authUser) {
        chatService.save(dto, authUser);

        return BaseResponse.success("파일 저장완료");
    }

}
