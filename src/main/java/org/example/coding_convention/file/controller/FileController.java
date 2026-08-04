package org.example.coding_convention.file.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.coding_convention.common.response.BaseResponse;
import org.example.coding_convention.file.model.FilesDto;
import org.example.coding_convention.file.service.FileService;
import org.example.coding_convention.user.model.UserDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.SQLException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/file")
@Tag(name = "File", description = "파일 관련 API")
public class FileController {

    private final FileService fileService;


    @Operation(
            summary = "파일 저장 기능",
            description = "파일 저장시 실행되는 기능"
    )
    @PostMapping("/register")
    public BaseResponse<String> register(@Valid @RequestBody FilesDto.Register dto) throws SQLException, IOException {
        fileService.save(dto);

        return BaseResponse.success("파일 저장완료");
    }


@Operation(
        summary = "파일 상세 조회기능",
        description = "파일 조회시 실행되는 기능"
)
    @GetMapping("/read/{idx}")
    public BaseResponse<FilesDto.FileContentRes> read(@PathVariable Integer idx, @AuthenticationPrincipal UserDto.AuthUser authUser) throws SQLException, IOException {
        FilesDto.FileContentRes result = fileService.readContentByIdx(idx, authUser);

        return BaseResponse.success(result);
    }


    @Operation(summary = "파일 내용 업데이트 (주기 저장용)",
            description = "fileName 에 해당하는 실제 저장 위치를 찾아 fileContents 로 업데이트")
    @PostMapping("/upload")
    public BaseResponse<String> upload(@RequestBody FilesDto.ContentUpdateReq req) {
        req.validate();
        fileService.updateContent(req);

        return BaseResponse.success("업로드 완료");
    }

    @DeleteMapping("/file/{fileIdx}")
    public BaseResponse<String> deleteFile(@PathVariable Integer fileIdx, @AuthenticationPrincipal UserDto.AuthUser authUser){
        fileService.deleteFile(fileIdx, authUser);
        return BaseResponse.success("삭제되었습니다.");
    }

}
