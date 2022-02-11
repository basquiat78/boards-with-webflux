package io.basquiat.boards.music.domain.vo;

import lombok.Data;

/**
 * music 관련 요청 정보를 담는 Value Ojbect
 * created by basquiat
 */
@Data
public class MusicVO {

    private Long musicianId;
    private String musicianName;
    private String instrument;
    private String birth;

    private String albumId;
    private String albumTitle;
    private String albumRelease;
    private String genre;

    private Long labelId;
    private String labelName;

}
