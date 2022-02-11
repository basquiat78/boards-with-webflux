package io.basquiat.boards.music.repository.custom.impl;

import io.basquiat.boards.music.domain.entity.Album;
import io.basquiat.boards.music.mapper.AlbumMapper;
import io.basquiat.boards.music.repository.custom.CustomAlbumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * CustomAlbumRepository 구현체
 * created by basquiat
 */
@RequiredArgsConstructor
public class CustomAlbumRepositoryImpl implements CustomAlbumRepository {

    private final R2dbcEntityTemplate query;

    private final AlbumMapper albumMapper;

    /**
     * created album
     * @param album
     * @return Mono<Album>
     */
    @Override
    public Mono<Album> insertAlbum(Album album) {
        return query.insert(album);
    }

    /**
     * 뮤지션 아이디로 조회된 앨범 정보를 반환한다.
     * @param id
     * @param pageable
     * @return Flux<Album>
     */
    @Override
    public Flux<Album> findAlbumByMusicianId(Long id, Pageable pageable) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT a.*, ");
        sb.append("       m.id AS musicianId, ");
        sb.append("       m.name AS musicianName, ");
        sb.append("       m.instrument, ");
        sb.append("       m.birth, ");
        sb.append("       l.id AS labelId, ");
        sb.append("       l.name AS labelName ");
        sb.append("     FROM album a");
        sb.append("     JOIN musician m ON a.musician_id = m.id");
        sb.append("     JOIN label l ON a.label_id = l.id");
        sb.append("    WHERE m.id = :id ");
        sb.append("    LIMIT :limit OFFSET :offset ");

        return query.getDatabaseClient().sql(sb.toString())
                                        .bind("id", id)
                                        .bind("limit", pageable.getPageSize())
                                        .bind("offset", pageable.getOffset())
                                        .map(albumMapper::apply)
                                        .all();
    }

}
