package io.basquiat.boards.meta.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * meta data entity
 * created by basquiat
 */
@Builder
@Data
@Table("meta_data")
@AllArgsConstructor
@RequiredArgsConstructor
public class MetaData {

    /** unique id */
    @Id
    @Column("id")
    private Long id;

    /** 메타 코드 */
    @Column("meta_code")
    private String metaCode;

    /** 등록일 */
    @Column("created_at")
    private LocalDateTime createdAt;

    /** 변경일 */
    @Column("updated_at")
    private LocalDateTime updatedAt;

}
