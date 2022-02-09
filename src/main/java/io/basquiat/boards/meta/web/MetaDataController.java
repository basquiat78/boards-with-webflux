package io.basquiat.boards.meta.web;

import io.basquiat.boards.meta.domain.MetaData;
import io.basquiat.boards.meta.service.MetaDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * MetaDataController
 * created by basquiat
 *
 * <pre>
 *     초간단 요청 컨트롤로로 metaCode를 받아서 저장하고 난 이후의 정보를 응답하는 API
 * </pre>
 */
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class MetaDataController {

    private final MetaDataService metaDataService;

    /**
     * metaCode 정보를 받아서 DB에 저장하고 결과값을 반환한다.
     * @param code
     * @return Mono<MetaData>
     */
    @PostMapping("/meta/{code}")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MetaData> createMetaData(@PathVariable("code") String code) {
        Assert.notNull(code, "must be code");
        return metaDataService.createMetaData(code);
    }

}
