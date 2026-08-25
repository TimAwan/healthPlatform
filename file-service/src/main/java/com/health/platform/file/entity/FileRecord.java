package com.health.platform.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("file_record")
public class FileRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long fileId;

    private String originalName;

    private String storageType;

    private String storagePath;

    private String bucket;

    private Long size;

    private String contentType;

    private String bizType;

    private Long uploaderId;

    private LocalDateTime createdAt;

    @TableLogic
    private Integer deleted;
}
