package io.basquiat.boards.music.repository.custom;

import io.basquiat.boards.music.domain.entity.Album;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * CustomAlbumRepository
 * created by basquiat
 */
public interface CustomAlbumRepository {

    Mono<Album> insertAlbum(Album album);

    Flux<Album> findAlbumByMusicianId(Long id, Pageable pageable);

}
