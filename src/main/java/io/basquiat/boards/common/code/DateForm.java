package io.basquiat.boards.common.code;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Arrays;

import static io.basquiat.boards.common.utils.DateUtils.localDateTimeToStringWithPattern;

/**
 * LocalDateTimeForm pattern
 * created by basquiat
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public enum DateForm {

    SIMPLE_YMD("yyyyMMdd"),
    SIMPLE_DASH_YMD("yyyy-MM-dd"),
    SIMPLE_DOT_YMD("yyyy.MM.dd"),
    SIMPLE_SPLASH_YMD("yyyy/MM/dd"),
    SIMPLE_YM("yyyyMM"),
    FULL_DATE("yyyy-MM-dd HH:mm:ss");

    @Getter
    private String pattern;

    /**
     * enum의 패턴에 맞춰서 LocalDateTime을 스트링으로 변환한다.
     * @param localDateTime
     * @return String
     */
    public String transform(LocalDateTime localDateTime) {
        return localDateTimeToStringWithPattern(localDateTime, this.pattern);
    }

    /**
     * 패턴이 없다면 YMD_WITH_MILLISECONDS을 기본으로 반환한다.
     * @param pattern
     * @return DateForm
     */
    public static DateForm of(String pattern) {
        return Arrays.stream(DateForm.values())
                     .filter(dateForm -> dateForm.getPattern().equalsIgnoreCase(pattern) )
                     .findAny()
                     .orElse(DateForm.FULL_DATE);
    }

}
