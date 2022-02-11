package io.basquiat.boards.music.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * musician entity
 * created by basquiat
 */
@Builder
@Data
@Table("musician")
@AllArgsConstructor
@RequiredArgsConstructor
public class Musician {

    /** unique id */
    @Id
    @Column("id")
    private Long id;

    /** 뮤지션 이름 */
    @Column("name")
    private String name;

    /** 뮤지션이 다루는 악기 정보 */
    @Column("instrument")
    private String instrument;

    /** 뮤지션의 생년월일 e.g: yyyyMMdd */
    @Column("birth")
    private String birth;

    /** 등록일 */
    @Column("created_at")
    private LocalDateTime createdAt;

    /** 갱신일 */
    @Column("updated_at")
    private LocalDateTime updatedAt;

}
