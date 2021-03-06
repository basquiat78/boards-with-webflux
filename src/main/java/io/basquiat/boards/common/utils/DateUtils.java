package io.basquiat.boards.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * date utils
 * created by basquiat
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateUtils {

    /**
     * pattern에 맞춰 LocalDateTime -> String 형식으로 변환
     * @param localDateTime
     * @param pattern
     * @return String
     */
    public static String localDateTimeToStringWithPattern(LocalDateTime localDateTime, String pattern) {
        try {
            return localDateTime.format(DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * String to LocalDate with Pattern
     * @param localDate
     * @param pattern
     * @return LocalDate
     */
    public static LocalDate localDateWithPattern(String localDate, String pattern) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        return LocalDate.parse(localDate, dateTimeFormatter);
    }


}