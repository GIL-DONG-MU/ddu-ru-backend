package com.dduru.gildongmu.common.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EnumUtils {

//     결과 캐싱 로직
//     private static final Map<Class<?>, Map<Integer, ? extends Enum<?>>> CACHE = new ConcurrentHashMap<>();
//     @SuppressWarnings("unchecked")                                                                                                   │
//            public static <E extends Enum<E> & CodedEnum> Map<Integer, E> codeMap(Class<E> enumClass) {                                  │
//                return (Map<Integer, E>) CACHE.computeIfAbsent(enumClass, cls ->
//                Arrays.stream(cls.getEnumConstants())                                                                 │
//                        .collect(Collectors.toMap(CodedEnum::getCode, Function.identity())));
//            }

    public static <E extends Enum<E> & CodedEnum> Map<Integer, E> codeMap(Class<E> enumClass) {
        // 미리 code를 key, Enum 상수를 value로 하는 Map을 만들어 반환
        return Arrays.stream(enumClass.getEnumConstants())
                .collect(Collectors.toMap(CodedEnum::getCode, Function.identity()));
    }

    public static <E extends Enum<E> & CodedEnum> Optional<E> fromCode(Class<E> enumClass, int code) {
        return Optional.ofNullable(codeMap(enumClass).get(code));
    }

    public static <T extends CodedEnum> Optional<T> findByCode(T[] enumValues, int code) {
        return Arrays.stream(enumValues)
                .filter(e -> e.getCode() == code)
                .findFirst();
    }
}
