package com.dduru.gildongmu.tag.controller;

import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tags")
public class TagController implements TagApiDocs {

    private final TagService tagService;

    @Override
    @GetMapping("/popular")
    public ResponseEntity<ApiResult<List<String>>> getPopularTags() {
        List<String> tags = tagService.getPopularTags();
        return ResponseEntity.ok(ApiResult.ok(tags));
    }
}
