package io.basquiat.boards.music.service;

import io.basquiat.boards.common.domain.SearchVO;
import io.basquiat.boards.music.domain.entity.Album;
import io.basquiat.boards.music.domain.entity.Label;
import io.basquiat.boards.music.domain.entity.Musician;
import io.basquiat.boards.music.domain.vo.MusicVO;
import io.basquiat.boards.music.repository.AlbumRepository;
import io.basquiat.boards.music.repository.LabelRepository;
import io.basquiat.boards.music.repository.MusicianRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static io.basquiat.boards.common.utils.CommonUtils.sorting;
import static java.time.LocalDateTime.now;

/**
 * music service
 * created by basquiat
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MusicService {

    private final MusicianRepository musicianRepository;
    private final AlbumRepository albumRepository;
    private final LabelRepository labelRepository;

    /**
     * 뮤지션을 조회한다.
     * @return Flux<Musician>
     */
    public Flux<Musician> fetchMusicians() {
        return musicianRepository.findAll();
    }

    /**
     * 뮤지션 정보를 생성한다.
     * @param musicVO
     * @return Mono<Musician>
     */
    public Mono<Musician> createMusician(MusicVO musicVO) {
        Musician newMusician = Musician.builder()
                                       .name(musicVO.getMusicianName())
                                       .instrument(musicVO.getInstrument())
                                       .birth(musicVO.getBirth())
                                       .createdAt(now())
                                       .build();
        return musicianRepository.save(newMusician);
    }

    /**
     * 뮤지션 아이디로 뮤지션의 앨범 정보가 담긴 뮤지션의 정보를 반환한다.
     * @param id
     * @return Mono<Musician>
     */
    public Mono<Musician> fetchMusicianById(Long id) {
        return musicianRepository.findMusicianById(id);
    }

    /**
     * 뮤지션의 앨범 정보가 담긴 뮤지션들의 정보를 반환한다.
     * @return Flux<Musician>
     */
    public Flux<Musician> musiciansAllInfo() {
        return musicianRepository.findMusicians();
    }

    /**
     * 앨범 정보를 생성한다.
     * @param musicVO
     * @return Mono<Album>
     */
    public Mono<Album> createAlbum(MusicVO musicVO) {
        Album newAlbum = Album.builder()
                              .id(UUID.randomUUID().toString())
                              .musicianId(musicVO.getMusicianId())
                              .title(musicVO.getAlbumTitle())
                              .releaseYear(musicVO.getAlbumRelease())
                              .genre(musicVO.getGenre())
                              .labelId(musicVO.getLabelId())
                              .createdAt(now())
                              .build();
        return albumRepository.insertAlbum(newAlbum);
    }

    /**
     * album 정보를 가져온다.
     * mapper 사용
     * @param musicianId
     * @param search
     * @return Flux<Album>
     */
    public Flux<Album> fetchAlbumsByMusician(Long musicianId, SearchVO search) {
        Pageable pageable = PageRequest.of(search.getPage(), search.getSize(), sorting(search.getSort(), search.getOrder()));
        return albumRepository.findAlbumByMusicianId(musicianId, pageable);
    }

    /**
     * album 정보를 가져온다.
     * converter 사용
     * @param musicianId
     * @param search
     * @return Flux<Album>
     */
    public Flux<Album> fetchAlbumsByMusicianCovert(Long musicianId, SearchVO search) {
        Pageable pageable = PageRequest.of(search.getPage(), search.getSize(), sorting(search.getSort(), search.getOrder()));
        return albumRepository.findAll(musicianId, pageable.getPageSize(), pageable.getOffset());
    }

    /**
     * 음반사를 조회한다.
     * @return Flux<Musician>
     */
    public Flux<Label> fetchLabels() {
        return labelRepository.findAll();
    }

    /**
     * pageable과 함께 음반사를 조회한다.
     * @return Flux<Musician>
     */
    public Flux<Label> fetchLabelsWithPageable(SearchVO search) {
        Pageable pageable = PageRequest.of(search.getPage(), search.getSize(), sorting(search.getSort(), search.getOrder()));
        return labelRepository.findLabelsWithPageable(pageable, search.getSearchValue());
    }

    /**
     * pageable과 함께 음반사를 조회한다.
     *
     * Page의 구현체인 PageImpl에 조회된 리스트 정보와 페이징 처리 정보를 담아서 보내기 때문에 Mono로 반환한다.
     *
     * @return Mono<Page<Label>>
     */
    public Mono<Page<Label>> fetchLabelsByWithPageable(SearchVO search) {
        Pageable pageable = PageRequest.of(search.getPage(), search.getSize(), sorting(search.getSort(), search.getOrder()));
        return labelRepository.findAllBy(pageable)
                              .collectList() // 1. 리스트로 만들고
                              .zipWith(labelRepository.count()) // 2. 전체 토탈카운트를 구하고
                              .map(t -> new PageImpl<>(t.getT1(), pageable, t.getT2())); // 3. 최종적으로 PageImpl에 해당 조회 정보와 페이징처리 정보를 세팅해서 반환한다.
    }

    /**
     * 음반 레이블 정보를 생성한다.
     * @param labelName
     * @return Mono<Label>
     */
    public Mono<Label> createLabel(String labelName) {
        Label newLabel = Label.builder()
                              .name(labelName)
                              .createdAt(now())
                              .build();
        return labelRepository.save(newLabel);
    }

}
