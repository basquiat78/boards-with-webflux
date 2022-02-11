package io.basquiat.boards.music.web;

import io.basquiat.boards.common.domain.SearchVO;
import io.basquiat.boards.music.domain.entity.Album;
import io.basquiat.boards.music.domain.entity.Label;
import io.basquiat.boards.music.domain.entity.Musician;
import io.basquiat.boards.music.domain.vo.MusicVO;
import io.basquiat.boards.music.service.MusicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Music API Controller
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/music")
public class MusicController {

    private final MusicService musicService;

    /**
     * 뮤지션 정보를 생성한다.
     * @param musicVO
     * @return Mono<Musician>
     */
    @PostMapping("/musicians")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Musician> createMusician(@RequestBody MusicVO musicVO) {
        return musicService.createMusician(musicVO);
    }

    /**
     * 뮤지션들의 정보를 반환한다.
     * @return Flux<Musician>
     */
    @GetMapping("/musicians")
    @ResponseStatus(HttpStatus.OK)
    public Flux<Musician> musicians() {
        return musicService.fetchMusicians();
    }

    /**
     * 앨범 정보가 담긴 뮤지션들의 정보를 반환한다.
     * @return Flux<Musician>
     */
    @GetMapping("/musicians/all")
    @ResponseStatus(HttpStatus.OK)
    public Flux<Musician> musiciansAllInfo() {
        return musicService.musiciansAllInfo();
    }

    /**
     * 앨범 정보가 담긴 뮤지션의 정보를 반환한다.
     * @param id
     * @return Mono<Musician>
     */
    @GetMapping("/musicians/id/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Musician> musicians(@PathVariable("id") Long id) {
        return musicService.fetchMusicianById(id);
    }

    /**
     * 앨범 정보를 생성한다.
     * @param musicVO
     * @return Mono<Album>
     */
    @PostMapping("/albums")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Album> createAlbum(@RequestBody MusicVO musicVO) {
        return musicService.createAlbum(musicVO);
    }

    /**
     * 특정 뮤지션의 앨범리스트를 반환한다.
     * mapper 사용
     * @param musicianId
     * @param search
     * @return Flux<Album>
     */
    @GetMapping("/albums/musician/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Flux<Album> fetchAlbumsByMusician(@PathVariable("id") Long musicianId, SearchVO search) {
        return musicService.fetchAlbumsByMusician(musicianId, search);
    }

    /**
     * 특정 뮤지션의 앨범리스트를 반환한다.
     * convert 사용
     * @param musicianId
     * @param search
     * @return Flux<Album>
     */
    @GetMapping("/albums/musician/{id}/convert")
    @ResponseStatus(HttpStatus.OK)
    public Flux<Album> fetchAlbumsByMusicianConvert(@PathVariable("id") Long musicianId, SearchVO search) {
        return musicService.fetchAlbumsByMusicianCovert(musicianId, search);
    }

    /**
     * 음반 레코드 사의 정보를 생성한다.
     * @param musicVO
     * @return Mono<Label>
     */
    @PostMapping("/labels")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Label> createLabel(@RequestBody MusicVO musicVO) {
        return musicService.createLabel(musicVO.getLabelName());
    }

    /**
     * 그냥 무식하게 다 조회한다.
     * @return Flux<Label>
     */
    @GetMapping("/labels/all")
    @ResponseStatus(HttpStatus.OK)
    public Flux<Label> labelsAll() {
        return musicService.fetchLabels();
    }

    /**
     * Pageable을 적용해서 조회한다.
     * @return Flux<Label>
     */
    @GetMapping("/labels/pageable")
    @ResponseStatus(HttpStatus.OK)
    public Flux<Label> labelsWithPageable(SearchVO searchVO) {
        return musicService.fetchLabelsWithPageable(searchVO);
    }

    /**
     * Pageable을 적용해서 조회한다.
     * @return Mono<Page<Label>>
     */
    @GetMapping("/labels/pageable/by")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Page<Label>> labelsByWithPageable(SearchVO searchVO) {
        return musicService.fetchLabelsByWithPageable(searchVO);
    }

}
