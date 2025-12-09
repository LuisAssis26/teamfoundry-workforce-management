package com.teamfoundry.backend.superadmin.controller.website;

import com.teamfoundry.backend.superadmin.dto.home.other.SiteMediaUploadResponse;
import com.teamfoundry.backend.superadmin.service.home.HomeMediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/super-admin/site/media")
@RequiredArgsConstructor
public class WebsiteMediaController {

    private final HomeMediaService mediaService;

    @PostMapping("/upload")
    public SiteMediaUploadResponse upload(@RequestParam("file") MultipartFile file) {
        return new SiteMediaUploadResponse(mediaService.storeImage(file));
    }
}
