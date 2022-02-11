package io.basquiat.boards.music.repository;

import io.basquiat.boards.music.domain.entity.Musician;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

/**
 * musician repository
 * created by basquiat
 */
public interface MusicianRepository extends ReactiveCrudRepository<Musician, Long>, CustomMusicianRepository {
}
