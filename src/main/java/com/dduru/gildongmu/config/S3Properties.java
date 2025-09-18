package com.dduru.gildongmu.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "aws.s3")
public class S3Properties {
    private String region;
    private String bucket;
    private String accessKey;
    private String secretKey;
}
