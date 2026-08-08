package com.dduru.gildongmu.home.dto.response;

public record HomeHostResponse(
        String nickname,
        ProfileImage profileImageInfo,
        int age,
        Gender gender
) {

    public enum Gender {
        M,
        F,
        U
    }

    public record ProfileImage(
            ImageType type,
            String url,
            Long bgColorId
    ) {
    }

    public enum ImageType {
        AVATAR,
        UPLOADED,
        DEFAULT
    }
}
