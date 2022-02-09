package io.basquiat.boards.meta.service;

import io.basquiat.boards.meta.domain.MetaData;
import io.basquiat.boards.meta.repository.MetaDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static java.time.LocalDateTime.now;

/**
 * MetaDataService
 * created by basquiat
 *
 * <pre>
 *     이 서비스는 어플리케이션 설정 확인을 위한 서비스로
 *     metaCode를 받아와 DB에 인서트하는 간단한 로직만 하나 만든다.
 *     이것이 제대로 된다면 다른 것도 잘 된다는 가정을 깔고 가는 것이다.
 * </pre>
 */
@Service
@RequiredArgsConstructor
public class MetaDataService {

    private final MetaDataRepository metaDataRepository;

    /**
     * 넘겨받은 metaCode를 디비에 새로 생성한다.
     * @param metaCode
     * @return Mono<MetaData>
     */
    public Mono<MetaData> createMetaData(String metaCode) {
        MetaData metaData = MetaData.builder()
                                    .metaCode(metaCode)
                                    .createdAt(now())
                                    .build();
        return metaDataRepository.save(metaData);
    }

}
