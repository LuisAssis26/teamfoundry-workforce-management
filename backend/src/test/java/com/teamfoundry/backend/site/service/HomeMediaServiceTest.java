package com.teamfoundry.backend.site.service;

import com.teamfoundry.backend.common.service.CloudinaryService;
import com.teamfoundry.backend.superadmin.service.home.HomeMediaService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HomeMediaServiceTest {

    @Test
    void storeImageUploadsToCloudinary() {
        CloudinaryService cloudinary = mock(CloudinaryService.class);
        HomeMediaService service = new HomeMediaService(cloudinary);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "example.png",
                "image/png",
                new byte[]{0x1, 0x2, 0x3}
        );

        when(cloudinary.uploadBytes(any(byte[].class), eq("home"), eq("example.png")))
                .thenReturn(new CloudinaryService.UploadResult("image::home/example", "https://cdn.test/home/example.png"));

        String url = service.storeImage(file);

        assertEquals("https://cdn.test/home/example.png", url);
        verify(cloudinary).uploadBytes(any(byte[].class), eq("home"), eq("example.png"));
    }

    @Test
    void rejectNonImageFiles() {
        CloudinaryService cloudinary = mock(CloudinaryService.class);
        HomeMediaService service = new HomeMediaService(cloudinary);

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.txt",
                "text/plain",
                "hello".getBytes()
        );

        assertThrows(ResponseStatusException.class, () -> service.storeImage(file));
    }
}
