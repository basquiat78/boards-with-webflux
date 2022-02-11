package io.basquiat.boards.common.code;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;

import java.util.function.Function;

/**
 * sort할 키를 받아서 Sort객체를 반환한다.
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public enum SortEnum {

    DESC((order) -> Sort.by(order).descending()),
    ASC((order) -> Sort.by(order).ascending());

    /** funtional interface set */
    private Function<String, Sort> function;

    /**
     * sort할 컬럼 명을 받아서 Sort객체를 반환한다.
     * @param order
     * @return Sort
     */
    public Sort sort(String order) {
        return function.apply(order);
    }

}
