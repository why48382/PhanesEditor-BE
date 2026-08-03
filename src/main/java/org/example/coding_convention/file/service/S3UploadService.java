package org.example.coding_convention.file.service;

import io.awspring.cloud.s3.S3Operations;
import io.awspring.cloud.s3.S3Resource;
import lombok.RequiredArgsConstructor;
import org.example.coding_convention.utils.FileUploadUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

@Service
@RequiredArgsConstructor
public class S3UploadService implements UploadService {
    @Value("${spring.cloud.aws.s3.bucket}")
    private String s3BucketName;
    private final S3Operations s3Operations;

    @Override
    public String upload(String fileName, String fileContents) throws SQLException, IOException {
        String dirPath = FileUploadUtil.makeUploadPath();
        InputStream inputStream = new ByteArrayInputStream(fileContents.getBytes(StandardCharsets.UTF_8));
        S3Resource s3Resource = s3Operations.upload(s3BucketName, dirPath + fileName, inputStream);
        return s3Resource.getURL().toString();
    }

    public String upload(Integer projectIdx, String fileName, String fileContents) throws SQLException, IOException {
        String dirPath = FileUploadUtil.makeProjectFilePath(projectIdx);
        InputStream inputStream = new ByteArrayInputStream(fileContents.getBytes(StandardCharsets.UTF_8));
        S3Resource s3Resource = s3Operations.upload(s3BucketName, dirPath + fileName, inputStream);
        return s3Resource.getURL().toString();
    }


}
