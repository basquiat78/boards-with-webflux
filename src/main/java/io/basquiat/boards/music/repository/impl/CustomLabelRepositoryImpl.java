package io.basquiat.boards.music.repository.impl;

import io.basquiat.boards.music.domain.entity.Label;
import io.basquiat.boards.music.repository.CustomLabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Query;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@RequiredArgsConstructor
public class CustomLabelRepositoryImpl implements CustomLabelRepository {

    private final R2dbcEntityTemplate query;

    public Flux<Label> findLabelsWithPageable(Pageable pageable, String searchValue) {
        Query initQuery = Query.empty();
        if(StringUtils.hasLength(searchValue)) {
            initQuery = query(where("name").like("%" + searchValue + "%"));
        }
        return query.select(Label.class)
                    .matching(initQuery.limit(pageable.getPageSize())
                                       .offset(pageable.getOffset())
                                       .sort(pageable.getSort()))
                    .all();

    }

}