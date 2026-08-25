package com.health.platform.file.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.health.platform.core.exception.BizException;
import com.health.platform.core.result.CommonErrorCode;
import com.health.platform.file.entity.FileRecord;
import com.health.platform.file.mapper.FileRecordMapper;
import com.health.platform.file.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

// TODO: 文件访问权限细化（说明书第 24/40 章）：一期文件仅医生资质附件且仅管理端使用，
// 上传/下载仅要求登录；二期客户报告上传上线前必须按业务类型与角色细化访问控制
@Slf4j
@Service
@RequiredArgsConstructor
public class FileManageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "pdf");

    private final FileRecordMapper fileRecordMapper;
    private final FileStorageService fileStorageService;

    @Value("${health.file.max-size-bytes:10485760}")
    private long maxSizeBytes;

    public FileRecord upload(MultipartFile file, String bizType, Long uploaderId) {
        if (file == null || file.isEmpty()) {
            throw new BizException("上传文件不能为空");
        }
        if (file.getSize() > maxSizeBytes) {
            throw new BizException("文件大小超过限制 " + (maxSizeBytes / 1024 / 1024) + "MB");
        }
        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BizException("不支持的文件类型: " + extension);
        }

        String fileId = UUID.randomUUID().toString().replace("-", "");
        String storagePath;
        try {
            storagePath = fileStorageService.store(fileId, file.getInputStream(),
                    file.getSize(), file.getContentType());
        } catch (IOException e) {
            log.error("读取上传文件失败: {}", e.getMessage());
            throw new BizException(CommonErrorCode.SYSTEM_ERROR, "文件读取失败");
        }

        FileRecord record = new FileRecord();
        record.setFileId(fileId);
        record.setOriginalName(file.getOriginalFilename());
        record.setStorageType(fileStorageService.storageType());
        record.setStoragePath(storagePath);
        record.setBucket(null);
        record.setSize(file.getSize());
        record.setContentType(file.getContentType());
        record.setBizType(bizType);
        record.setUploaderId(uploaderId);
        fileRecordMapper.insert(record);
        return record;
    }

    public FileRecord download(String fileId) {
        return requireFile(fileId);
    }

    public java.io.InputStream openStream(FileRecord record) {
        return fileStorageService.read(record.getStoragePath());
    }

    public void delete(String fileId) {
        FileRecord record = requireFile(fileId);
        fileStorageService.delete(record.getStoragePath());
        fileRecordMapper.deleteById(record.getId());
    }

    private FileRecord requireFile(String fileId) {
        FileRecord record = fileRecordMapper.selectOne(new LambdaQueryWrapper<FileRecord>()
                .eq(FileRecord::getFileId, fileId));
        if (record == null) {
            throw new BizException(CommonErrorCode.NOT_FOUND, "文件不存在");
        }
        return record;
    }

    private String extensionOf(String filename) {
        if (!StringUtils.hasText(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
