package io.basquiat.boards.music.mapper;

import io.basquiat.boards.music.domain.entity.Album;
import io.basquiat.boards.music.domain.entity.Label;
import io.basquiat.boards.music.domain.entity.Musician;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.function.BiFunction;

/**
 * album mapper
 *
 * Row 데이터를 Album entity로 매핑하는 매퍼, 또는 별도의 DTO를 생성해서 사용해도 무방하다.
 *
 * created by basquiat
 */
@Component
public class AlbumMapper implements BiFunction<Row, RowMetadata, Album> {

    @Override
    public Album apply(Row row, RowMetadata rowMetadata) {
        return Album.builder()
                    .id(row.get("id").toString())
                    .musicianId(row.get("musicianId", Long.class))
                    .title(row.get("title").toString())
                    .releaseYear(row.get("release_year").toString())
                    .genre(row.get("genre").toString())
                    .labelId(row.get("labelId", Long.class))
                    .createdAt(row.get("created_at", LocalDateTime.class))
                    .updatedAt(row.get("updated_at", LocalDateTime.class))
                    .musician(Musician.builder()
                                      .id(row.get("musicianId", Long.class))
                                      .name(row.get("musicianName").toString())
                                      .instrument(row.get("instrument").toString())
                                      .birth(row.get("birth").toString())
                                      .build())
                    .label(Label.builder()
                                .id(row.get("labelId", Long.class))
                                .name(row.get("labelName").toString())
                                .build())
                    .build();
    }

}
