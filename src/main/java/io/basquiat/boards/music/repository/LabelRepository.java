package io.basquiat.boards.music.repository;

import io.basquiat.boards.music.domain.entity.Label;
import io.basquiat.boards.music.repository.custom.CustomLabelRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Flux;

/**
 * label repository
 * created by basquiat
 */
public interface LabelRepository extends ReactiveSortingRepository<Label, Long>, CustomLabelRepository {

    Flux<Label> findAllBy(Pageable pageable);

}
