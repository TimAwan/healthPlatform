package com.health.platform.file.controller;

import com.health.platform.core.result.Result;
import com.health.platform.file.entity.FileRecord;
import com.health.platform.file.service.FileManageService;
import com.health.platform.file.vo.FileVO;
import com.health.platform.security.context.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileManageService fileManageService;

    @PostMapping
    public Result<FileVO> upload(@RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "bizType", required = false) String bizType) {
        Long uploaderId = UserContext.currentUserIdOrNull();
        FileRecord record = fileManageService.upload(file, bizType, uploaderId);
        FileVO vo = new FileVO();
        vo.setFileId(record.getFileId());
        vo.setOriginalName(record.getOriginalName());
        vo.setSize(record.getSize());
        vo.setContentType(record.getContentType());
        vo.setBizType(record.getBizType());
        return Result.ok(vo);
    }

    @GetMapping("/{fileId}")
    public Result<FileVO> info(@PathVariable String fileId) {
        FileRecord record = fileManageService.download(fileId);
        FileVO vo = new FileVO();
        vo.setFileId(record.getFileId());
        vo.setOriginalName(record.getOriginalName());
        vo.setSize(record.getSize());
        vo.setContentType(record.getContentType());
        vo.setBizType(record.getBizType());
        return Result.ok(vo);
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable String fileId) {
        FileRecord record = fileManageService.download(fileId);
        String encodedName = URLEncoder.encode(record.getOriginalName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (record.getContentType() != null) {
            try {
                mediaType = MediaType.parseMediaType(record.getContentType());
            } catch (Exception ignored) {
            }
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedName)
                .contentType(mediaType)
                .body(new InputStreamResource(fileManageService.openStream(record)));
    }

    @DeleteMapping("/{fileId}")
    public Result<Void> delete(@PathVariable String fileId) {
        fileManageService.delete(fileId);
        return Result.ok();
    }
}
