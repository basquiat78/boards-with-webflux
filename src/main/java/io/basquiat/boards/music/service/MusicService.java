package io.basquiat.boards.music.service;

import io.basquiat.boards.common.domain.SearchVO;
import io.basquiat.boards.music.domain.entity.Label;
import io.basquiat.boards.music.domain.entity.Musician;
import io.basquiat.boards.music.domain.vo.MusicVO;
import io.basquiat.boards.music.repository.LabelRepository;
import io.basquiat.boards.music.repository.MusicianRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
