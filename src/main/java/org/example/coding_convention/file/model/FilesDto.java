package org.example.coding_convention.file.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.example.coding_convention.project.model.Project;

import java.util.List;

public class FilesDto {
    @Getter
    public static class ContentUpdateReq {
        private String fileName;
        private String fileContents;
        private Integer projectId;

        public void validate() {
            if (fileName == null || fileName.isBlank()) {
                throw new IllegalArgumentException("fileName 은 필수입니다.");
            }
            if (fileContents == null) {
                throw new IllegalArgumentException("fileContents 는 null 일 수 없습니다. (빈 문자열은 허용)");
            }
            if (projectId == null) {
                throw new IllegalArgumentException("projectId 는 필수입니다.");
            }
        }
    }


    @Getter
    @Builder
    public static class FilesList {
        private Integer idx;
        private String name;

        public static FilesDto.FilesList from(Files filesEntity) {
            return FilesDto.FilesList.builder()
                    .idx(filesEntity.getIdx())
                    .name(filesEntity.getName())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class FilesRes {
        private String name;
        private Files.FileType type;

        public static FilesDto.FilesRes from(Files entity) {
            FilesRes dto = FilesRes.builder()
                    .name(entity.getName())
                    .type(entity.getType())
                    .build();

            return dto;

        }
    }

    @Setter
    @Getter
    @Schema(name = "File.Register", description = "파일등록 기능")
    public static class Register {
        @Schema(description = "프로젝트 id 자동 주입", example = "1")
        private Integer idx;

        @NotBlank(message = "파일 이름은 비어있을 수 없습니다.")
//        @Pattern(
//                regexp = "^[a-zA-Z0-9._-]+$",
//                message = "파일 이름에는 /, \\, :, *, ?, \", <, >, | 같은 특수문자를 사용할 수 없습니다."
//        )
        @Schema(description = "파일 이름 작성", example = "show.txt")
        private String name;
        @Schema(description = "파일에 대한 간단한 설명", example = "코테연습")
        private String contents; // 저장할 때 해당 파일에 대한 내용도 함께 받기
        @Schema(description = "update를 위해서 필요, 최초 생성 시 null 가능", example = "10")
        private Integer fileIdx;


        public Files toEntity(String filetype, String URL) {


            Project project = Project.builder()
                    .idx(idx)
                    .build();

            if(this.fileIdx == null) {
                Files entity = Files.builder()
                        .project(project)
                        .name(this.name)
                        .URL(URL)
                        .path(URL.replaceAll(".*\\.com/", ""))
                        .type(Files.setFileType(filetype))
                        .build();
                return entity;
            }

            Files entity = Files.builder()
                    .idx(fileIdx)
                    .project(project)
                    .name(this.name)
                    .URL(URL)
                    .path(URL.replaceAll(".*\\.com/", ""))
                    .type(Files.setFileType(filetype))
                    .build();

            return entity;
        }
    }

//    public static class FileUpload {
//        private String fileName;
//        private String fileContents;
//        private Integer projectId;
//
//        public Files toEntity(String filetype, String URL) {
//
//        }
//    }

    @Getter
    @Builder
    public static class FileContentRes {
        private Integer idx;
        private String name;
        private String path;
        private Files.FileType type;
        private String contents; // 실제 S3 파일 내용

        // 편의 메서드
        public static FileContentRes of(Files entity, String contents) {
            return FileContentRes.builder()
                    .idx(entity.getIdx())
                    .name(entity.getName())
                    .path(entity.getPath())
                    .type(entity.getType()) // enum이면 .name()
                    .contents(contents)
                    .build();
        }
    }

}
