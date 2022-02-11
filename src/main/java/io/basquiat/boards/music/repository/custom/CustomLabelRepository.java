package io.basquiat.boards.music.repository.custom;

import io.basquiat.boards.music.domain.entity.Label;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;

/**
 * CustomLabelRepository
 * created by basquiat
 */
public interface CustomLabelRepository {

    Flux<Label> findLabelsWithPageable(Pageable pageable, String searchValue);

}
