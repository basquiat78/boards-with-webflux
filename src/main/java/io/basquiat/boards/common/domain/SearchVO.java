package io.basquiat.boards.common.domain;

import lombok.Data;

/**
 * query param mapping object
 * created by basquiat
 */
@Data
public class SearchVO {

    private int page;
    private int size;
    private String sort;
    private String order;
    private String searchValue;

    public int getSize() {
        if(size == 0) {
            return 10;
        }
        return this.size;
    }

}
