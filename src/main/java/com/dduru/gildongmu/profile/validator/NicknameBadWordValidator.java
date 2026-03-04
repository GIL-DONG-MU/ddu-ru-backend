package com.dduru.gildongmu.profile.validator;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class NicknameBadWordValidator {

    private static final List<String> BAD_WORDS = List.of("쓰레기"); // TODO: 교체 예정

    public static boolean validate(String nickname) {
        for (String badWord : BAD_WORDS) {
            if (nickname.contains(badWord)) {
                return false;
            }
        }
        return true;
    }
}
