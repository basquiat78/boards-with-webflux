package io.basquiat.boards.music.repository;

import io.basquiat.boards.music.domain.entity.Album;
import io.basquiat.boards.music.repository.custom.CustomAlbumRepository;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

/**
 * album repository
 * created by basquiat
 */
public interface AlbumRepository extends ReactiveCrudRepository<Album, String>, CustomAlbumRepository {

    @Query("SELECT a.*, m.id AS musicianId, m.name AS musicianName, m.instrument, m.birth, l.id AS labelId, l.name AS labelName FROM album a JOIN musician m ON a.musician_id = m.id JOIN label l ON a.label_id = l.id WHERE m.id = :id LIMIT :limit OFFSET :offset")
    Flux<Album> findAll(@Param("id") Long id, @Param("limit") int limit, @Param("offset") Long offset);

}
