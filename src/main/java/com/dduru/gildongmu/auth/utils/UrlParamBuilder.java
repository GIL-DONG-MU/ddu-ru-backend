package com.dduru.gildongmu.auth.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class UrlParamBuilder {

    private final StringBuilder params = new StringBuilder();

    public static UrlParamBuilder create() {
        return new UrlParamBuilder();
    }

    public UrlParamBuilder add(String key, String value) {
        if (value != null) {
            if (!params.isEmpty()) {
                params.append("&");
            }
            params.append(encode(key)).append("=").append(encode(value));
        }
        return this;
    }

    public String build() {
        return params.toString();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
