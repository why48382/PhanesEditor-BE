package org.example.coding_convention.file.service;

import lombok.RequiredArgsConstructor;
import org.example.coding_convention.file.model.Files;
import org.example.coding_convention.file.model.FilesDto;
import org.example.coding_convention.file.repository.FileRepository;
import org.example.coding_convention.project.model.Project;
import org.example.coding_convention.user.model.User;
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
import software.amazon.awssdk.services.s3.model.S3Location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileService {
    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final S3UploadService s3UploadService;
    private final S3Client s3Client; //S3 사용을 위한 객체 추가

    @Transactional
    public void updateContent(FilesDto.ContentUpdateReq req) {
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


    public void save(FilesDto.Register dto) throws SQLException, IOException {

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
//        // 현재 로그인 한 사용자가 이 파일에 대한 권한이 있는지 확인
//        Integer userIdx = authUser.getIdx();
//        User user = userRepository.findById(userIdx)
//                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다. idx=" + userIdx));
//
//        // 해당 사용자의 프로젝트들의 파일을 확인
//        boolean isHave = false;
//        for (Project project : user.getProjectList()) {
//            for (Files file : project.getFileList()) {
//                if (file.getIdx().equals(idx)) {
//                    isHave = true;
//                    break; // 찾으면 루프 종료
//                }
//            }
//            if (isHave) break;
//        }
//
//        // 권한 없으면 바로 예외 처리
//        if (!isHave) {
//            throw new IllegalArgumentException("해당 파일에 접근 권한이 없습니다. fileIdx=" + idx);
//        }

        // 권한이 있는 경우에만 파일 엔티티 조회
        Files entity = fileRepository.findById(idx)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다. idx=" + idx));

        String uri = entity.getURL();

        try {
            String contents;

            if (uri.startsWith("s3://")) {
                // S3 URI -> SDK로 읽기
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
                // 퍼블릭/프리사인드 URL -> HTTP로 바로 읽기
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

    // s3://bucket/key 전용
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
}
