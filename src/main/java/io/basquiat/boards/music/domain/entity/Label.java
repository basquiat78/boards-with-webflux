package io.basquiat.boards.music.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * label entity
 * created by basquiat
 */
@Builder
@Data
@Table("label")
@AllArgsConstructor
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Label {

    /** unique id */
    @Id
    @Column("id")
    private Long id;

    /** 레이블 명 */
    @Column("name")
    private String name;

    /** 등록일 */
    @Column("created_at")
    private LocalDateTime createdAt;

    /** 갱신일 */
    @Column("updated_at")
    private LocalDateTime updatedAt;

}
