package org.example.coding_convention.file.service;

import lombok.RequiredArgsConstructor;
import org.example.coding_convention.file.model.Files;
import org.example.coding_convention.file.model.FilesDto;
import org.example.coding_convention.file.repository.FileRepository;
import org.example.coding_convention.project.model.Project;
import org.example.coding_convention.project_member.repository.ProjectMemberRepository;
import org.example.coding_convention.user.model.UserDto;
import org.example.coding_convention.user.repository.UserRepository;
import org.example.coding_convention.utils.S3UrlUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;
    private final S3UploadService s3UploadService;
    private final S3Client s3Client; //S3 사용을 위한 객체 추가
    private final ProjectMemberRepository projectMemberRepository;

    @Transactional
    public void updateContent(FilesDto.ContentUpdateReq req, UserDto.AuthUser authUser) {
        boolean isMember = projectMemberRepository.existsByProject_IdxAndUser_Idx(req.getProjectId(), authUser.getIdx());
        if (!isMember) {
            throw new RuntimeException("해당 프로젝트의 멤버만 파일을 수정할 수 있습니다.");
        }

        Files entity = fileRepository.findByPath(req.getFileName()).orElseThrow(() -> new IllegalArgumentException("File not found"));

        String url = entity.getURL();
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("이 파일은 URL이 등록되어 있지 않습니다");
        }

        // 2) S3 bucket/key 파싱
        var parts = S3UrlUtils.parse(url);
        byte[] bytes = req.getFileContents().getBytes(StandardCharsets.UTF_8);

        // 3) putObject(덮어쓰기)
        PutObjectRequest por = PutObjectRequest.builder()
                .bucket(parts.bucket())
                .key(parts.key())
                .contentType("text/plain; charset=utf-8")
                .contentLength((long) bytes.length)
                .build();

        s3Client.putObject(por, RequestBody.fromBytes(bytes));

        entity.setSaveTimeAt(LocalDateTime.now());
    }


    public void save(FilesDto.Register dto, UserDto.AuthUser authUser) throws SQLException, IOException {
        boolean isMember = projectMemberRepository.existsByProject_IdxAndUser_Idx(dto.getIdx(), authUser.getIdx());
        if (!isMember) {
            throw new RuntimeException("해당 프로젝트의 멤버만 파일을 생성할 수 있습니다.");
        }

        String URL = s3UploadService.upload(dto.getIdx(), dto.getName(), dto.getContents());

        String dtoName = dto.getName();
        if (dtoName.contains(".")) {
            String fileType = "FILE";
            fileRepository.save(dto.toEntity(fileType, URL));
        } else {
            String fileType = "DIRECTORY";
            fileRepository.save(dto.toEntity(fileType, URL));
        }
    }

    public FilesDto.FilesRes read(Integer idx) {
        Optional<Files> result = fileRepository.findById(idx);
        if (result.isPresent()) {

            Files entity = result.get();
            return FilesDto.FilesRes.from(entity);
        }
        return null;
    }

    @Transactional(readOnly = true)
    public FilesDto.FileContentRes readContentByIdx(Integer idx, UserDto.AuthUser authUser) {
        Files entity = fileRepository.findById(idx)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다. idx=" + idx));
        String uri = entity.getURL();
        try {
            String contents;
            if (uri.startsWith("s3://")) {
                S3Location loc = parseS3Uri(uri);
                try (ResponseInputStream<GetObjectResponse> is = s3Client.getObject(
                        GetObjectRequest.builder()
                                .bucket(loc.bucket())
                                .key(loc.key())
                                .build()
                )) {
                    contents = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                }
            } else if (uri.startsWith("http://") || uri.startsWith("https://")) {
                URL url = new URL(uri);
                try (BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line).append('\n');
                    contents = sb.toString();
                }
            } else {
                throw new IllegalArgumentException("지원하지 않는 URI 스킴: " + uri);
            }
            return FilesDto.FileContentRes.of(entity, contents);
        } catch (IOException e) {
            throw new RuntimeException("파일 읽기 실패: " + uri, e);
        }
    }


    @Transactional
    public void deleteFile(Integer fileIdx, UserDto.AuthUser authUser) {
        Files file = fileRepository.findById(fileIdx)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다."));

        boolean isMember = projectMemberRepository.existsByProject_IdxAndUser_Idx(
                file.getProject().getIdx(), authUser.getIdx());

        if (!isMember) {
            throw new RuntimeException("해당 프로젝트의 멤버만 파일을 삭제할 수 있습니다.");
        }

        renameForDelete(file);
        fileRepository.save(file);

        if (file.getType() == Files.FileType.DIRECTORY) {
            String prefix = file.getPath() + "/";
            List<Files> children = fileRepository.findByProject_IdxAndPathStartingWithAndDeletedFalse(
                    file.getProject().getIdx(), prefix);
            children.forEach(this::renameForDelete);
            fileRepository.saveAll(children);
        }
    }

    @Transactional
    public void createDefaultFile(Project project) throws SQLException, IOException {
        String fileName = defaultFileName(project.getLanguage());
        String contents = defaultFileContent(project.getLanguage(), project.getProjectName());

        String url = s3UploadService.upload(project.getIdx(), fileName, contents);

        Files entity = Files.builder()
                .project(project)
                .name(fileName)
                .URL(url)
                .path(url.replaceAll(".*\\.com/", ""))
                .type(Files.FileType.FILE)
                .build();

        fileRepository.save(entity);
    }

    private void renameForDelete(Files file) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
        file.setName(file.getName() + "_deleted_" + timestamp + "_" + file.getIdx());
        file.setDeleted(true);
    }

    private String defaultFileName(Project.Language language) {
        String ext = switch (language) {
            case JAVASCRIPT -> "js";
            case JAVA -> "java";
            case PYTHON -> "py";
            case C -> "c";
            case MARKDOWN -> "md";
        };
        return "Main." + ext;
    }

    private String defaultFileContent(Project.Language language, String projectName) {
        return switch (language) {
            case JAVA -> "public class Main {\n    public static void main(String[] args) {\n\n    }\n}\n";
            case C -> "#include <stdio.h>\n\nint main() {\n    return 0;\n}\n";
            case JAVASCRIPT -> "console.log(\"Hello, " + projectName + "!\");\n";
            case PYTHON -> "print(\"Hello, " + projectName + "!\")\n";
            case MARKDOWN -> "# " + projectName + "\n";
        };
    }

    private record S3Location(String bucket, String key) {
    }

    private S3Location parseS3Uri(String uri) {
        if (uri == null || !uri.startsWith("s3://")) {
            throw new IllegalArgumentException("유효한 S3 URI가 아닙니다: " + uri);
        }
        String rest = uri.substring(5); // "bucket/key..."
        int slash = rest.indexOf('/');
        if (slash <= 0 || slash == rest.length() - 1) {
            throw new IllegalArgumentException("S3 URI 파싱 실패: " + uri);
        }
        return new S3Location(rest.substring(0, slash), rest.substring(slash + 1));
    }

}
