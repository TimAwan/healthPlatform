package com.health.platform.file.vo;

import lombok.Data;

@Data
public class FileVO {

    private Long fileId;

    private String originalName;

    private Long size;

    private String contentType;

    private String bizType;
}
