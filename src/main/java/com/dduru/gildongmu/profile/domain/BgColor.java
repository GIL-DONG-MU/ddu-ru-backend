package com.dduru.gildongmu.profile.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bg_colors")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BgColor extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hex_code", nullable = false, length = 7)
    private String hexCode;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Builder
    public BgColor(String hexCode, Integer displayOrder) {
        this.hexCode = hexCode;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
    }
}
