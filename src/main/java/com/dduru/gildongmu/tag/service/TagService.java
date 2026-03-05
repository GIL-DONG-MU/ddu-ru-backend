package com.dduru.gildongmu.tag.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TagService {

    private static final List<String> POPULAR_TAGS = List.of(
            "힐링여행",
            "맛집투어",
            "사진명소",
            "액티비티",
            "자연"
    );

    public List<String> getPopularTags() {
        return POPULAR_TAGS;
    }
}
