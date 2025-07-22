package com.dduru.gildongmu.post.enums;

import lombok.Getter;

@Getter
public enum PostStatus {
    OPEN("게시글이 공개된 상태입니다."),
    FULL("게시글이 모집 완료된 상태입니다."),
    CLOSED("게시글이 마감된 상태입니다."),
    DELETED("게시글이 삭제된 상태입니다.");

    private final String description;

    PostStatus(String description) {
        this.description = description;
    }
}
