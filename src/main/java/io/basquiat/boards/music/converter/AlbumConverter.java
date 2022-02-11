package io.basquiat.boards.music.converter;

import io.basquiat.boards.music.domain.entity.Album;
import io.basquiat.boards.music.domain.entity.Label;
import io.basquiat.boards.music.domain.entity.Musician;
import io.r2dbc.spi.Row;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.time.LocalDateTime;

/**
 * album converter
 * 조회 이후 row객체를 ablum 엔티티로 변환한다. 또는 이 것을 통해서 정의한 DTO에 담을 수 도 있다.
 *
 * created by basquiat
 *
 */
@ReadingConverter
public class AlbumConverter implements Converter<Row, Album> {

    @Override
    public Album convert(Row row) {
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
