package com.health.platform.file.storage;

import com.health.platform.core.exception.BizException;
import com.health.platform.core.result.CommonErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 本地磁盘存储，开发环境使用（health.file.storage 缺省或 =local 时启用）。
 */
@Component
@ConditionalOnProperty(name = "health.file.storage", havingValue = "local", matchIfMissing = true)
public class LocalFileStorage implements FileStorageService {

    private final Path baseDir;

    public LocalFileStorage(@Value("${health.file.local.dir:./data/files}") String baseDir) {
        this.baseDir = Paths.get(baseDir).toAbsolutePath().normalize();
    }

    @Override
    public String store(String fileId, InputStream content, long size, String contentType) {
        try {
            Path target = resolve(fileId);
            Files.createDirectories(target.getParent());
            Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
            return fileId;
        } catch (IOException e) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "文件保存失败");
        }
    }

    @Override
    public InputStream read(String storagePath) {
        try {
            return Files.newInputStream(resolve(storagePath));
        } catch (IOException e) {
            throw new BizException(CommonErrorCode.NOT_FOUND, "文件不存在");
        }
    }

    @Override
    public void delete(String storagePath) {
        try {
            Files.deleteIfExists(resolve(storagePath));
        } catch (IOException e) {
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "文件删除失败");
        }
    }

    @Override
    public String storageType() {
        return "LOCAL";
    }

    /** 只允许访问 baseDir 内的路径，防目录穿越 */
    private Path resolve(String name) {
        Path target = baseDir.resolve(name).normalize();
        if (!target.startsWith(baseDir)) {
            throw new BizException(CommonErrorCode.PARAM_ERROR, "非法文件路径");
        }
        return target;
    }
}
