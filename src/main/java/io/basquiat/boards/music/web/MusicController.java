package io.basquiat.boards.music.web;

import io.basquiat.boards.common.domain.SearchVO;
import io.basquiat.boards.music.domain.entity.Label;
import io.basquiat.boards.music.domain.entity.Musician;
import io.basquiat.boards.music.domain.vo.MusicVO;
import io.basquiat.boards.music.service.MusicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     *
     * @return
     */
    @GetMapping("/musicians")
    @ResponseStatus(HttpStatus.OK)
    public Flux<Musician> musicians() {
        return musicService.fetchMusicians();
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

}
