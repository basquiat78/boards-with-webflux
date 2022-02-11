package io.basquiat.boards.music.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * album entity
 * created by basquiat
 */
@Builder
@Data
@Table("album")
@AllArgsConstructor
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Album {

    /** unique id */
    @Id
    @Column("id")
    private String id;

    /** 뮤지션 아이디 */
    @Column("musician_id")
    private Long musicianId;

    /** 앨범 타이틀 */
    @Column("title")
    private String title;

    /** 앨범 발매 년도 e.g: yyyy */
    @Column("release_year")
    private String releaseYear;

    /** 앨범의 음악 장르 */
    @Column("genre")
    private String genre;

    /** 앨범이 발매된 레이블 아이디 */
    @Column("label_id")
    private Long labelId;

    /** 등록일 */
    @Column("created_at")
    private LocalDateTime createdAt;

    /** 갱신일 */
    @Column("updated_at")
    private LocalDateTime updatedAt;

    private Musician musician;

    private Label label;

}
