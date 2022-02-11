package io.basquiat.boards.common.mapper;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * RowToMapMapper
 *
 * MySqlRowMetadata에서 컬럼을 이름으로 소팅하는 부분이 있어서 그냥 날코딩으로 fetch대신 이것을 사용한다.
 *
 * created by basquiat
 *
 */
@Component
public class RowToMapMapper implements BiFunction<Row, RowMetadata, Map<String, Object>> {

    /**
     * 순수한 형태로 row to Map으로 변환한다.
     * @param row
     * @param rowMetadata
     * @return Map<String, Object>
     */
    @Override
    public Map<String, Object> apply(Row row, RowMetadata rowMetadata) {
        Collection<String> columns = rowMetadata.getColumnNames();
        int columnCount = columns.size();
        Map<String, Object> mapOfColValues = new LinkedCaseInsensitiveMap<>(columnCount);
        for (String column : columns) {
            String key = column;
            Object obj = row.get(key);
            mapOfColValues.put(key, obj);
        }
        return mapOfColValues;
    }

}
