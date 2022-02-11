package io.basquiat.boards.music.repository;

import io.basquiat.boards.music.domain.entity.Album;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

/**
 * album repository
 * created by basquiat
 */
public interface AlbumRepository extends ReactiveCrudRepository<Album, String> {
}
