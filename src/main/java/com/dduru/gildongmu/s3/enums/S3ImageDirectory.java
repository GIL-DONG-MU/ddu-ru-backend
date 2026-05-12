package com.dduru.gildongmu.s3.enums;

public enum S3ImageDirectory {
    POSTS("posts"),
    PROFILES("profiles"),
    JOURNEYS("journeys"),
    JOURNEY_POSTS("journeys/posts"),
    CHATS("chats");

    private final String directory;

    S3ImageDirectory(String directory) {
        this.directory = directory;
    }

    public String keyPrefix() {
        return directory + "/";
    }

    public String uriPrefix() {
        return "/" + directory + "/";
    }
}
