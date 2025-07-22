package com.dduru.gildongmu.post.enums;

import com.dduru.gildongmu.post.domain.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "destinations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Destination {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "country_code", nullable = false, length = 2)
    private String countryCode;

    @Column(name = "country_name", nullable = false, length = 100)
    private String countryName;

    @Column(length = 100)
    private String region;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(length = 500)
    private String image;

    @Builder
    public Destination(String countryCode, String countryName, String region, String city, String image) {
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.region = region;
        this.city = city;
        this.image = image;
    }
}
