package io.basquiat.boards.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.basquiat.boards.common.code.SortEnum;
import io.basquiat.boards.common.exception.JsonConvertException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.text.MessageFormat;

/**
 * commmon utils
 * created by basquiat
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonUtils {

    private static ObjectMapper objectMapper = null;

    private static ObjectMapper mapper() {
        if(objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        }
        return objectMapper;
    }

    /**
     * 객체를 json string으로 변환한다.
     * @param object
     * @return String
     * @throws JsonProcessingException
     */
    public static String toJson(Object object) {
        String result;
        try {
            result = mapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonConvertException(e.getMessage());
        }
        return result;
    }

    /**
     * json String을 넘겨받은 객체 class타입으로 변환한다.
     *
     * @param content
     * @param clazz
     * @return T
     * @throws Exception
     */
    public static <T> T convertObject(String content, Class<T> clazz) {
        T object;
        try {
            object = mapper().readValue(content, clazz);
        } catch (IOException e) {
            throw new JsonConvertException(e.getMessage());
        }
        return object;
    }

    /**
     * Object을 넘겨받은 객체 class타입으로 deep copy를 해서 만들어서 변환한다.
     * @param object
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T convertObject(Object object, Class<T> clazz) {
        T obj;
        try {
            obj = mapper().readValue(toJson(object), clazz);
        } catch (IOException e) {
            throw new JsonConvertException(e.getMessage());
        }
        return obj;
    }

    /**
     * Generic Collection Type covert method
     * 만일 특정 객체의 컬렉션 타입인 경우 해당 메소드를 사용한다.
     * @param content
     * @param clazz
     * @return T
     * @throws Exception
     */
    public static <T> T convertObjectByTypeRef(String content, TypeReference<T> clazz) {
        T object;
        try {
            object = mapper().readValue(content, clazz);
        } catch (IOException e) {
            throw new JsonConvertException(e.getMessage());
        }
        return object;
    }

    /**
     * Generic Collection Type covert method
     * 만일 특정 객체의 컬렉션 타입인 경우 해당 메소드를 사용한다.
     * @param object
     * @param clazz
     * @return T
     * @throws Exception
     */
    public static <T> T convertObjectByTypeRef(Object object, TypeReference<T> clazz) {
        return convertObjectByTypeRef(toJson(object), clazz);
    }

    /**
     * 메세지 포맷을 이용한 스트링 생성
     * @param format
     * @param args
     * @return String
     */
    public static String fm(String format, Object...args) {
        return MessageFormat.format(format, args);
    }

    /**
     * Sort 객체를 반환한다.
     * @param sort
     * @param order
     * @return Sort
     */
    public static Sort sorting(String sort, String order) {
        // sort, order 정보가 없다면
        if(!StringUtils.hasLength(sort) || !StringUtils.hasLength(order)) {
            return Sort.unsorted();
        }
        try {
            return SortEnum.valueOf(sort).sort(order);
        } catch (Exception e) {
            // 실수로 sort 값이 enum에 정의되지 않는 값이 넘어온다면 로그만 남기고 그냥 소트하지 않는다.
            log.info("Sort키가 SortEnum에 정의되지 않는 값입니다. 확인하세요");
            return Sort.unsorted();
        }
    }

}
