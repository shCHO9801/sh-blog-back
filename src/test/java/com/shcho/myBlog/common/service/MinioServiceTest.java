package com.shcho.myBlog.common.service;

import com.shcho.myBlog.libs.exception.CustomException;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static com.shcho.myBlog.common.entity.UploadType.ATTACHMENT;
import static com.shcho.myBlog.common.entity.UploadType.IMAGE;
import static com.shcho.myBlog.libs.exception.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Minio Service Unit Test")
@ExtendWith(MockitoExtension.class)
class MinioServiceTest {

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private MinioService minioService;

    @Test
    @DisplayName("업로드 실패 - 파일이 비어있음")
    void uploadFailedFileEmpty() {
        // given
        MultipartFile emptyFile = mock(MultipartFile.class);
        when(emptyFile.isEmpty()).thenReturn(true);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> minioService.upload(1L, emptyFile, IMAGE));
        assertEquals(FILE_EMPTY, exception.getErrorCode());
        verifyNoInteractions(minioClient);
    }

    @Test
    @DisplayName("업로드 실패 - 이미지 용량 초괴")
    void uploadFailedImageTooLarge() {
        // given
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(10L * 1024 * 1024 + 1);

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> minioService.upload(1L, file, IMAGE));

        assertEquals(FILE_TOO_LARGE, exception.getErrorCode());
        verifyNoInteractions(minioClient);
    }

    @Test
    @DisplayName("업로드 실패 - 이미지 확장자 불가")
    void uploadFailedInvalidImageExtension() {
        // given
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("evil.exe");

        // when & then
        CustomException ex = assertThrows(CustomException.class,
                () -> minioService.upload(1L, file, IMAGE));

        assertEquals(INVALID_FILE_EXTENSION, ex.getErrorCode());
        verifyNoInteractions(minioClient);
    }

    @Test
    @DisplayName("업로드 실패 - 첨부파일 확장자 불가")
    void uploadFailedInvalidAttachmentExtension() {
        // given
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("script.sh");

        // when & then
        CustomException ex = assertThrows(CustomException.class,
                () -> minioService.upload(1L, file, ATTACHMENT));

        assertEquals(INVALID_FILE_EXTENSION, ex.getErrorCode());
        verifyNoInteractions(minioClient);
    }

    @Test
    @DisplayName("업로드 성공 - putObject 호출 및 URL 반환")
    void uploadSuccess() throws Exception {
        // given
        ReflectionTestUtils.setField(minioService, "bucket", "shblog");
        ReflectionTestUtils.setField(minioService, "minioBaseUrl", "https://minio.shhome.synology.me");

        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getSize()).thenReturn(1024L);
        when(file.getOriginalFilename()).thenReturn("test.jpg");
        when(file.getContentType()).thenReturn("image/jpeg");

        InputStream is = new ByteArrayInputStream("dummy".getBytes());
        when(file.getInputStream()).thenReturn(is);

        when(minioClient.putObject(any(PutObjectArgs.class)))
                .thenReturn(mock(ObjectWriteResponse.class));

        // when
        String url = minioService.upload(1L, file, IMAGE);

        // then
        assertNotNull(url);
        assertTrue(url.startsWith("https://minio.shhome.synology.me/shblog/users/1/images/"));
        assertTrue(url.endsWith(".jpg"));

        ArgumentCaptor<PutObjectArgs> captor = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient, times(1)).putObject(captor.capture());

        PutObjectArgs args = captor.getValue();
        assertEquals("shblog", args.bucket());
        assertNotNull(args.object());
        assertTrue(args.object().contains("users/1/images/"));
    }
}