package org.example.coding_convention.user.service;

import io.awspring.cloud.s3.S3Template;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.coding_convention.user.model.EmailVerify;
import org.example.coding_convention.user.model.User;
import org.example.coding_convention.user.model.UserDto;
import org.example.coding_convention.user.repository.EmailVerifyRepository;
import org.example.coding_convention.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final S3Template s3Template;
    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;
    private final UserRepository userRepository;
    private final JavaMailSender emailSender;
    private final EmailVerifyRepository emailVerifyRepository;
    private final PasswordEncoder passwordEncoder;

//    private final PasswordEncoder passwordEncoder;

    public UserDto.Profile findProfile(UserDto.AuthUser authUser) {
        User entity = userRepository.findById(authUser.getIdx()).orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UserDto.Profile result = UserDto.Profile.from(entity);
        return result;
    }

    @Transactional
    public String updateImage(MultipartFile file, UserDto.AuthUser authUser) {
        User user = userRepository.findById(authUser.getIdx())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        try {
            // S3에 업로드 (InputStream 업로드)
            String key = "profiles/" + authUser.getIdx() + "/"
                    + UUID.randomUUID() + "_" + file.getOriginalFilename();

            s3Template.upload(bucketName, key, file.getInputStream());

            // 업로드된 객체 URL 만들기
            String imageUrl = String.format("https://%s.s3.%s.amazonaws.com/%s",
                    bucketName, "ap-northeast-2", key);

            // DB에 저장
            user.setProfileImg(imageUrl);

            return imageUrl;
        } catch (IOException e) {
            throw new RuntimeException("이미지 업로드 실패", e);
        }
    }




    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> result = userRepository.findByEmail(email);

        if(result.isPresent()) {
            User user = result.get();
            return UserDto.AuthUser.from(user);
        }
        return null;
    }


    public void signup(UserDto.Register dto) throws MessagingException {
        User user = dto.toEntity();
        user.updatePassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

//        User user = userRepository.save(dto.toEntity());
        // 메일 전송
        String uuid = UUID.randomUUID().toString();

        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(dto.getEmail());
        String subject = "[Phanes Editor] 가입 환영";
        String htmlContent = "<h2 style='color: #2e6c80;'>가입을 환영합니다!</h2>"
                + "<p>아래 링크를 클릭하여 이메일 인증을 완료해주세요:</p>"
                + "<a href='http://localhost:8080/user/verify?uuid=" + uuid + "'>이메일 인증하기</a>";
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        emailSender.send(message);

        // 랜덤한 값을 DB에 저장
        EmailVerify emailVerify= EmailVerify.builder()
                .uuid(uuid)
                .user(user)
                .build();
        emailVerifyRepository.save(emailVerify);
    }

    public void verify(String uuid) {
        Optional<EmailVerify> result = emailVerifyRepository.findByUuid(uuid);

        if(result.isPresent()) {
            EmailVerify emailVerify = result.get();
            User user = emailVerify.getUser();
            user.userVerify();
            userRepository.save(user);
        } else {
            log.debug("인증 실패");
        }
    }

    public Optional<User> findByNickName(String nickname) {
        return userRepository.findByNickname(nickname);
    }

    public List<UserDto.UserSearch> userSearch(String nickname) {
        List<User> result = userRepository.findByNicknameLike("%" + nickname + "%");
        return result.stream().map(UserDto.UserSearch::from).toList();
    }
}
