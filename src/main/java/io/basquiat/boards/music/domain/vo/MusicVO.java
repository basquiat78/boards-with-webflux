package io.basquiat.boards.music.domain.vo;

import lombok.Data;

@Data
public class MusicVO {

    private Long musicianId;
    private String musicianName;
    private String instrument;
    private String birth;

    private String albumId;
    private String albumTile;
    private String albumRelease;
    private String genre;

    private Long labelId;
    private String labelName;

}
