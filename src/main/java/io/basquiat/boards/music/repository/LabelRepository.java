package io.basquiat.boards.music.repository;

import io.basquiat.boards.music.domain.entity.Label;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

/**
 * label repository
 * created by basquiat
 */
public interface LabelRepository extends ReactiveCrudRepository<Label, Long>, CustomLabelRepository {
}
