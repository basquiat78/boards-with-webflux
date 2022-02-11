package io.basquiat.boards.music.repository.custom.impl;

import io.basquiat.boards.common.mapper.RowToMapMapper;
import io.basquiat.boards.music.domain.entity.Album;
import io.basquiat.boards.music.domain.entity.Label;
import io.basquiat.boards.music.domain.entity.Musician;
import io.basquiat.boards.music.repository.custom.CustomMusicianRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static io.basquiat.boards.common.utils.DateUtils.toDateTime;
import static io.basquiat.boards.common.utils.NumberUtils.parseLong;
import static java.util.stream.Collectors.toList;

/**
 * CustomMusicianRepository 구현체
 * created by basquiat
 */
@Slf4j
@RequiredArgsConstructor
public class CustomMusicianRepositoryImpl implements CustomMusicianRepository {

    private final R2dbcEntityTemplate query;

    private final RowToMapMapper rowToMapMapper;

    /**
     * 뮤지션 아이디로 앨범 정보를 포함한 뮤지션의 정보를 반환한다.
     * @param id
     * @return Mono<Musician>
     */
    @Override
    public Mono<Musician> findMusicianById(Long id) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT musician.id AS id, ");
        sb.append("       musician.name AS name, ");
        sb.append("       musician.instrument AS instrument, ");
        sb.append("       musician.birth AS birth, ");
        sb.append("       musician.created_at AS created, ");
        sb.append("       musician.updated_at AS updated, ");
        sb.append("       album.id AS albumId, ");
        sb.append("       album.title AS title, ");
        sb.append("       album.release_year AS releaseYear, ");
        sb.append("       album.genre AS genre, ");
        sb.append("       label.id AS labelId, ");
        sb.append("       label.name AS labelName ");
        sb.append("     FROM musician");
        sb.append("     LEFT JOIN album ON musician.id = album.musician_id");
        sb.append("     JOIN label ON album.label_id = label.id");
        sb.append("    WHERE musician.id = :id ");

        return query.getDatabaseClient().sql(sb.toString())
                                        .bind("id", id)
                                        .map(rowToMapMapper::apply)
                                        .all()
                                        .bufferUntilChanged(result -> result.get("id"))
                                        .map(rows ->
                                                Musician.builder()
                                                        .id(parseLong(rows.get(0).get("id")))
                                                        .name(rows.get(0).get("name").toString())
                                                        .instrument(rows.get(0).get("instrument").toString())
                                                        .birth(rows.get(0).get("birth").toString())
                                                        .createdAt(toDateTime(rows.get(0).get("created")))
                                                        .updatedAt(toDateTime(rows.get(0).get("updated")))
                                                        .albums(rows.stream()
                                                                    .map(row -> Album.builder()
                                                                            .id(row.get("albumId").toString())
                                                                            .title(row.get("title").toString())
                                                                            .releaseYear(row.get("releaseYear").toString())
                                                                            .genre(row.get("genre").toString())
                                                                            .label(Label.builder()
                                                                                        .id(parseLong(row.get("labelId")))
                                                                                        .name(row.get("labelName").toString())
                                                                                        .build())
                                                                            .build())
                                                                    .collect(toList()))
                                                        .build()
                                        )
                                        .take(1)
                                        .next();
    }

    /**
     * 앨범 정보를 포함한 뮤지션들의 정보를 반환한다.
     * @return Flux<Musician>
     */
    @Override
    public Flux<Musician> findMusicians() {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT musician.id, ");
        sb.append("       musician.name, ");
        sb.append("       musician.instrument, ");
        sb.append("       musician.birth, ");
        sb.append("       musician.created_at AS created, ");
        sb.append("       musician.updated_at AS updated, ");
        sb.append("       album.id AS albumId, ");
        sb.append("       album.title AS title, ");
        sb.append("       album.release_year AS releaseYear, ");
        sb.append("       album.genre AS genre, ");
        sb.append("       label.id AS labelId, ");
        sb.append("       label.name AS labelName ");
        sb.append("     FROM musician");
        sb.append("     LEFT JOIN album ON musician.id = album.musician_id");
        sb.append("     JOIN label ON album.label_id = label.id");
        sb.append("     ORDER BY musician.id");

        return query.getDatabaseClient().sql(sb.toString())
                                        .map(rowToMapMapper::apply)
                                        .all()
                                        .bufferUntilChanged(result -> result.get("id"))
                                        .map(rows ->
                                                Musician.builder()
                                                        .id(parseLong(rows.get(0).get("id")))
                                                        .name(rows.get(0).get("name").toString())
                                                        .instrument(rows.get(0).get("instrument").toString())
                                                        .birth(rows.get(0).get("birth").toString())
                                                        .createdAt(toDateTime(rows.get(0).get("created")))
                                                        .updatedAt(toDateTime(rows.get(0).get("updated")))
                                                        .albums(rows.stream()
                                                                .map(row -> Album.builder()
                                                                                 .id(row.get("albumId").toString())
                                                                                 .title(row.get("title").toString())
                                                                                 .releaseYear(row.get("releaseYear").toString())
                                                                                 .genre(row.get("genre").toString())
                                                                                 .label(Label.builder()
                                                                                             .id(parseLong(row.get("labelId")))
                                                                                             .name(row.get("labelName").toString())
                                                                                 .build())
                                                                        .build())
                                                                .collect(toList()))
                                                        .build()
                                        );
    }

}
