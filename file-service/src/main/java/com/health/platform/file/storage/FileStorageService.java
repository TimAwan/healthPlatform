package com.health.platform.file.storage;

import java.io.InputStream;

/**
 * 文件存储抽象（说明书第 7 章：业务模块不得直接依赖 OSS SDK，便于未来替换 MinIO 等）。
 */
public interface FileStorageService {

    /** 存储文件，返回存储定位（本地相对路径或 OSS object key） */
    String store(String fileId, InputStream content, long size, String contentType);

    /** 读取文件内容 */
    InputStream read(String storagePath);

    /** 删除物理文件 */
    void delete(String storagePath);

    /** 当前存储类型 LOCAL/OSS */
    String storageType();
}
