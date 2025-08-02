package com.dduru.gildongmu.auth.dto.google;

public record PersonExtendedInfo(String gender, String ageRange, String phoneNumber) {

    public static PersonExtendedInfo empty() {
        return new PersonExtendedInfo(null, null, null);
    }
}
