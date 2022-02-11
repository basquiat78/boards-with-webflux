package io.basquiat.boards.music.repository;

import io.basquiat.boards.music.domain.entity.Musician;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * CustomMusicianRepository
 * created by basquiat
 */
public interface CustomMusicianRepository {

    Mono<Musician> findMusicianById(Long id);

    Flux<Musician> findMusicians();

}
