package com.dduru.gildongmu.auth.utils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public final class UrlParamBuilder {

    private final StringBuilder params = new StringBuilder();


    public UrlParamBuilder add(String key, String value) {
        if (value != null) {
            if (!params.isEmpty()) {
                params.append("&");
            }
            params.append(URLEncoder.encode(key, StandardCharsets.UTF_8)).append("=").append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        }
        return this;
    }

    public String build() {
        return params.toString();
    }

}
