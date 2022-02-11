package io.basquiat.boards.common.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Number Utils
 * created by basquiat
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NumberUtils {

    /**
     * convert Long
     * @param value
     * @return Long
     */
    public static Long parseLong(Object value) {
        if(value == null) {
            return Long.valueOf(0);
        }
        if(value instanceof Long) {
            return (Long)value;
        }
        if(value instanceof Integer) {
            return ((Integer)value).longValue();
        }
        if(value instanceof String) {
            try {
                return Long.parseLong((String)value);
            } catch (Exception e) {
                return Long.valueOf(0);
            }
        }
        return Long.valueOf(0);
    }

    /**
     * convert Integer
     * @param value
     * @return Integer
     */
    public static Integer parseInteger(Object value) {
        if(value == null) {
            return Integer.valueOf(0);
        }
        if(value instanceof Integer) {
            return (Integer)value;
        }
        if(value instanceof String) {
            try {
                return Integer.parseInt((String)value);
            } catch (Exception e) {
                return Integer.valueOf(0);
            }
        }
        return Integer.valueOf(0);
    }

}