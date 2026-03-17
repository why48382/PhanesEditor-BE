package org.example.coding_convention.likes.controller;

import lombok.RequiredArgsConstructor;
import org.example.coding_convention.common.response.BaseResponse;
import org.example.coding_convention.likes.service.LikeService;
import org.example.coding_convention.user.model.UserDto;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/like")
@RequiredArgsConstructor
public class LikeController {
    private final LikeService likeService;

    @GetMapping("/{idx}")
    public BaseResponse likes(@AuthenticationPrincipal UserDto.AuthUser authUser, @PathVariable Integer idx) {
        likeService.likes(authUser, idx);

        return BaseResponse.success("완료");
    }
}
