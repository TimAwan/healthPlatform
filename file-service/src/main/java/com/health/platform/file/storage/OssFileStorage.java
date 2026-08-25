package com.health.platform.file.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * 阿里云 OSS 存储，生产环境使用（health.file.storage=oss 时启用）。
 * 密钥经环境变量 OSS_ENDPOINT / OSS_ACCESS_KEY / OSS_SECRET_KEY 注入（说明书第 36 章）。
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "health.file.storage", havingValue = "oss")
public class OssFileStorage implements FileStorageService {

    private final OSS ossClient;
    private final String bucket;

    public OssFileStorage(@Value("${OSS_ENDPOINT:}") String endpoint,
                          @Value("${OSS_ACCESS_KEY:}") String accessKey,
                          @Value("${OSS_SECRET_KEY:}") String secretKey,
                          @Value("${OSS_BUCKET:}") String bucket) {
        this.bucket = bucket;
        this.ossClient = new OSSClientBuilder().build(endpoint, accessKey, secretKey);
        log.info("OSS 存储已初始化 bucket={}", bucket);
    }

    @Override
    public String store(String fileId, InputStream content, long size, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(size);
        if (contentType != null) {
            metadata.setContentType(contentType);
        }
        ossClient.putObject(bucket, fileId, content, metadata);
        return fileId;
    }

    @Override
    public InputStream read(String storagePath) {
        return ossClient.getObject(bucket, storagePath).getObjectContent();
    }

    @Override
    public void delete(String storagePath) {
        ossClient.deleteObject(bucket, storagePath);
    }

    @Override
    public String storageType() {
        return "OSS";
    }

    @PreDestroy
    public void shutdown() {
        ossClient.shutdown();
    }
}
