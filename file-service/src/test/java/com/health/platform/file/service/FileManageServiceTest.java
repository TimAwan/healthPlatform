package com.health.platform.file.service;

import com.health.platform.core.exception.BizException;
import com.health.platform.file.entity.FileRecord;
import com.health.platform.file.mapper.FileRecordMapper;
import com.health.platform.file.storage.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileManageServiceTest {

    @Mock
    private FileRecordMapper fileRecordMapper;

    @TempDir
    Path tempDir;

    private FileManageService fileManageService;

    @BeforeEach
    void setUp() {
        com.health.platform.file.storage.LocalFileStorage storage =
                new com.health.platform.file.storage.LocalFileStorage(tempDir.toString());
        fileManageService = new FileManageService(fileRecordMapper, storage);
        ReflectionTestUtils.setField(fileManageService, "maxSizeBytes", 1024L);
    }

    private MultipartFile file(String name, byte[] content) {
        MultipartFile multipart = mock(MultipartFile.class);
        org.mockito.Mockito.lenient().when(multipart.getOriginalFilename()).thenReturn(name);
        org.mockito.Mockito.lenient().when(multipart.getSize()).thenReturn((long) content.length);
        org.mockito.Mockito.lenient().when(multipart.getContentType()).thenReturn("application/octet-stream");
        try {
            org.mockito.Mockito.lenient().when(multipart.getInputStream()).thenReturn(new ByteArrayInputStream(content));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return multipart;
    }

    @Test
    void uploadShouldStoreAndRecordMetadata() {
        byte[] content = "hello".getBytes();
        MultipartFile multipart = file("report.png", content);

        FileRecord record = fileManageService.upload(multipart, "DOCTOR_ATTACHMENT", 9L);

        assertEquals(32, record.getFileId().length());
        assertEquals("report.png", record.getOriginalName());
        assertEquals("LOCAL", record.getStorageType());
        assertEquals((long) content.length, record.getSize());
        assertEquals("DOCTOR_ATTACHMENT", record.getBizType());
        verifyInsertCalledWith(record);
        assertArrayEquals(content, readStored(record));
    }

    @Test
    void uploadShouldRejectDisallowedExtension() {
        BizException e = assertThrows(BizException.class,
                () -> fileManageService.upload(file("virus.exe", new byte[1]), null, null));
        assertTrue(e.getMessage().contains("不支持的文件类型"));
    }

    @Test
    void uploadShouldRejectOversizedFile() {
        MultipartFile multipart = file("big.png", new byte[0]);
        org.mockito.Mockito.lenient().when(multipart.getSize()).thenReturn(2048L);
        BizException e = assertThrows(BizException.class,
                () -> fileManageService.upload(multipart, null, null));
        assertTrue(e.getMessage().contains("文件大小超过限制"));
    }

    @Test
    void uploadShouldRejectEmptyFile() {
        MultipartFile multipart = file("empty.png", new byte[0]);
        org.mockito.Mockito.lenient().when(multipart.isEmpty()).thenReturn(true);
        assertThrows(BizException.class, () -> fileManageService.upload(multipart, null, null));
    }

    private void verifyInsertCalledWith(FileRecord record) {
        ArgumentCaptor<FileRecord> captor = ArgumentCaptor.forClass(FileRecord.class);
        org.mockito.Mockito.verify(fileRecordMapper).insert(captor.capture());
        assertEquals(record.getFileId(), captor.getValue().getFileId());
    }

    private byte[] readStored(FileRecord record) {
        try {
            return Files.readAllBytes(tempDir.resolve(record.getStoragePath()));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
