package io.basquiat.boards.meta.repository;

import io.basquiat.boards.meta.domain.MetaData;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

/**
 * MetaDataRepository
 * created by basquiat
 */
public interface MetaDataRepository extends ReactiveCrudRepository<MetaData, Long> {
}
