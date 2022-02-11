# 시작하기 앞서서

WebFlux는 고전적인 방식의 컨트롤러 방식과 nodeJs처럼 함수형 인터페이스인 RouterFunction의 구현체를 할용해서 일종의 api router역할을 분리하는 Functional Endpoint방식이 존재한다.

Functional Endpoint방식은 api 진입 부분을 분리하면서 좀 더 비지니스 로직에 특화되어 있는 것처럼 보이지만 몇가지 어노테이션을 활용한 막강한 기능들을 사용하는데 약간 제한적이다.

실제로 이렇게 구성해놨다가 결국 컨트롤러 방식으로 바꿨던 경험을 생각한다면 경량 API에는 잘 어울리지 않을까 하는 생각이 든다.

어째든 이것은 개발자의 입맛, 서버의 목적에 따른 선택지이지만 여기서는 고전적인 방식의 컨트롤러 방식을 선택한다.

## gradle 재설정

일단 집에서 사용하는 노트북이랑 외부에서 들고다니는 노트북이 달라서 git을 클론해서 작업할려고 했더니 외부 노트북의 그레이들 버전이 낮았다.

최소 6.8 버전을 사용해야 하기 때문에 먼저 gradle 설정을 좀 바꿔야 한다.

gradle폴더에 gradle-wrapper.properties 파일을 열어보면 

distributionUrl부분을 보면
```
distributionUrl=https\://services.gradle.org/distributions/gradle-6.3-bin.zip
```

'와 한동안 이 노트북 사용을 안했구나'

https://gradle.org/releases/ 사이트를 가보면 현재 7.4버전이 있다. 

위 파일에서

```
distributionUrl=https\://services.gradle.org/distributions/gradle-7.3-bin.zip
```
이렇게 버전을 바꾼다. 나는 7.3으로 작업할 예정이다.

보통 인텔리제이에서 Preference > Build, Execution, Deployment > Build Tools > Gradle로 들어가면

하단부에 Gradle > Use Gradle From : 'gradle-wrapper.properties' file 이라고 되어 있을텐데

이렇게 세팅되어 있다면 그대로 놔두자.

터미널에서

```
> ./gradlew wrapper --gradle-version 7.3
Downloading https://services.gradle.org/distributions/gradle-7.3-bin.zip
...........10%...........20%...........30%...........40%...........50%...........60%...........70%...........80%...........90%...........100%

Welcome to Gradle 7.3!

Here are the highlights of this release:
 - Easily declare new test suites in Java projects
 - Support for Java 17
 - Support for Scala 3

For more details see https://docs.gradle.org/7.3/release-notes.html

Starting a Gradle Daemon (subsequent builds will be faster)

BUILD SUCCESSFUL in 24s
1 actionable task: 1 executed
```

가 되면서 그레이들 버전이 업데이트 된다.

## 좀더 어플리케이션 답게

아무래도 어플리케이션은 개인적으로 가장 중요한 것은 잘 만드는것도 중요하지만 얘기치 못한 트러블슈팅이다.

그러기 위애서는 가장 기본적이고 중요한 로깅일 것이다.

예전에 이런 생각을 한 적이 있었는데

'웹플럭스를 쓴다면 로그 남기는 것도 뭔가 비동기적이어야 하는거 아냐?'

하지만 그렇다고 로그를 남길 때 'AsyncAppender'를 활용하면 되겠지라는 생각을 해다면 잠시 접어두자.

모든 것은 기본에 충실해야 한다.

~~RollingFileAppender을 사용하는 걸로~~

로그 설정은 application.yml과 logback-spring.xml을 확인하자.

로그 스타일은 입맛에 맞게 커스텀 하면 된다.

## 뮤지션의 정보를 담아보자

다음 테이블을 생성한다.

좀 더 복잡할 수 있지만 뮤지션의 정보를 담는 테이블이다.

기본적인 뮤지션의 정보와 해당 뮤지션의 음반 정보, 그리고 음반이 발매된 레이블의 정보를 담는 심플한 테이블이다.

악기와 장르의 경우에는 멀티 유저가 있을 수 있고 장르도 하위 장르로 포함할 수 있기 때문에 구분자 ','라 집어넣을 수 있게 구성한다.

```
CREATE TABLE `musician` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(128) NOT NULL COMMENT '뮤지션 이름',
  `instrument` varchar(256) NULL DEFAULT NULL COMMENT '뮤지션이 다루는 악기 정보, 멀티 유져가 있을 수 있다',
  `birth` varchar(8) NULL DEFAULT NULL COMMENT '뮤지션의 생년월일 e.g: yyyyMMdd',
  `created_at` datetime NULL DEFAULT NULL COMMENT '등록일',
  `updated_at` datetime NULL DEFAULT NULL COMMENT '갱신일',
  PRIMARY KEY (`id`)
)


CREATE TABLE `album` (
  `id` varchar(45) NOT NULL COMMENT '앨범 아이디 uuid',
  `musician_id` int NOT NULL COMMENT '뮤지션 아이디',
  `title` varchar(256) NOT NULL COMMENT '앨범 타이틀',
  `release` varchar(6) NULL DEFAULT NULL COMMENT '앨범 발매 년도 e.g: yyyyMM',
  `genre` varchar(128) Null DEFAULT NULL COMMENT '앨범의 음악 장르, 하위 장르도 존재할 수 있다.',
  `label_id` int NULL DEFAULT NULL COMMENT '앨범이 발매된 레이블 아이디',
  `created_at` datetime NULL DEFAULT NULL COMMENT '등록일',
  `updated_at` datetime NULL DEFAULT NULL COMMENT '갱신일',
  PRIMARY KEY (`id`)
)

CREATE TABLE `label` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '레이블 명',
  `created_at` datetime NULL DEFAULT NULL COMMENT '등록일',
  `updated_at` datetime NULL DEFAULT NULL COMMENT '갱신일',
  PRIMARY KEY (`id`)
)
```

foreign key는 잡지 않았다. 일단 테이블과 뮤지션을 생성하는 간단한 테스트 코드를 작성하자.

프로젝트에서 io.basquiat.boards.music패캐지를 참조하자.     

참고로 나의 경우에는 다음과 같은 정보를 넣었다.

좋아하는 뮤지션과 레이블이 있다면 락이든 힙합이든 재즈든 넣고싶은데로 막 넣어두자.

물론 API 작동여부를 확인했다면 직접 sql을 넣어서 넣든 일단 데이터를 만들어 보자.


```
// 뮤지션 정보
{
    "musicianName" : "John Coltrane",
    "instrument" : "Tenor Saxophone, Soprano Saxophone, Bass Clarinet",
    "birth" : "19260923"
}
{
    "musicianName" : "Charlie Parker",
    "instrument" : "Alto Saxophone",
    "birth" : "19200829"
}
{
    "musicianName" : "Pat Metheny",
    "instrument" : "Guitars",
    "birth" : "19540812"
}
{
    "musicianName" : "Ornette Coleman",
    "instrument" : "Alto Saxophone, Bass Clarinet, Violin, Trumpet",
    "birth" : "19300319"
}


// 음반사 정보
{"labelName": "ECM"}
{"labelName": "Riverside"}
{"labelName": "GRP"}
{"labelName": "BlueNote"}
{"labelName": "ACT"}
{"labelName": "Nonesuch"}
{"labelName": "Label Bleu"}
{"labelName": "VerVenus Recordsve"}
{"labelName": "Edition Records"}
{"labelName": "Fresh Sound New Talent"}
{"labelName": "MackAvenue"}
{"labelName": "Verve"}
{"labelName": "Sunnyside"}
```

물론 모든 뮤지션별, 레이블별 조회하는 API도 만들어서 테스트를 한다.

MusicCotroller의 labelsAll을 참조하자.

하지만 어디 어플리케이션이 이렇게 모든 조회를 조건없이 조회한다는 것은 생각할 수 없다.

그렇다면 페이징을 처리해야 한다. ~~sorting도~~

페이징을 처리하는 방식은 고전적인 방식의 Pageable을 사용하는 방식과 R2dbcEntityTemplate를 활용한 Criteria형식으로 처리하는 방식이 있다.

최신 버전에서는 R2dbcEntityTemplate가 DatabaseClint의 Wrapper라고 했었는데 과거 버전에서는 DatabaseClint를 통해서 Criteria형식을 사용했었지만 기능적인 부분을 분리시킨듯 하다. 


그렇다면 R2dbcEntityTemplate로 어떻게 구현하는지 한번 트라이 해보자

R2dbcEntityTemplate를 이용해 criteria로 구성할때는 약간의 꼼수가 필요하다.

CustomLabelRepository.java
```
package io.basquiat.boards.music.repository;

import io.basquiat.boards.music.domain.entity.Label;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;

public interface CustomLabelRepository {

    Flux<Label> findLabelsWithPageable(Pageable pageable);

}

```

CustomLabelRepositoryImpl.java
```
package io.basquiat.boards.music.repository.impl;

import io.basquiat.boards.music.domain.entity.Label;
import io.basquiat.boards.music.repository.CustomLabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Flux;

import static org.springframework.data.relational.core.query.Query.query;

@RequiredArgsConstructor
public class CustomLabelRepositoryImpl implements CustomLabelRepository {

    private final R2dbcEntityTemplate query;

    public Flux<Label> findLabelsWithPageable(Pageable pageable) {
        return query.select(Label.class)
                    .matching(query(null).limit(pageable.getPageSize())
                                         .offset(pageable.getOffset())
                                         .sort(pageable.getSort()))
                    .all();

    }

}
```

위 코드를 보면 matching부분에 메소드체인으로 limit/offset/sort를 통해 적용시킨다.    
일단은 조회할 조건을 두지 않았기 때문에 null로 적용을 하게 되면 조건없이 쿼리가 생성된다.

queryDsl처럼 @Nullable이 걸려있어서 가능하다.

그럼 컨트롤러와 서비스 부분을 한번 삺펴보자.

MusicController.java
```
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
    public Flux<Label> labelsWithPageabl(SearchVO searchVO) {
        return musicService.fetchLabelsWithPageable(searchVO);
    }

}

```
사실 좀 지저분하긴 하지만 labelsWithPageabl메소드에 4개의 queryParam을 받는다.

MusicService.java
```
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

```
fetchLabelsWithPageable메소드에서 볼수 있듯이 Pageable을 생성한다. 소트할 정보가 있다면 소트 정보도 넣어주는 코드이다.

이것은 에제로 이것저것 테스트하는 코드이기 때문에 필요에 따라서 사용하면 되는 부분이므로 필요없다면 과감하게 커스텀하면 된다.

SortEumn을 생성하고 CommonUtils에

```
    /**
     * Sort 객체를 반환한다.
     * @param sort
     * @param order
     * @return Sort
     */
    public static Sort sorting(String sort, String order) {
        // sort, order 정보가 없다면
        if(!StringUtils.hasLength(sort) || !StringUtils.hasLength(order)) {
            return Sort.unsorted();
        }
        try {
            return SortEnum.valueOf(sort).sort(order);
        } catch (Exception e) {
            // 실수로 sort 값이 enum에 정의되지 않는 값이 넘어온다면 로그만 남기고 그냥 소트하지 않는다.
            log.info("Sort키가 SortEnum에 정의되지 않는 값입니다. 확인하세요");
            return Sort.unsorted();
        }
    }
```
메소도를 만들어서 편리하게 사용하자.

localhost:8080/api/music/labels/pageable?page=0&size=5&sort=ASC&order=id

처럼 이것저것 값을 수정해서 보내며 원하는대로 작동을 잘 하는지 학인하자.

최종적으로 다이나믹하게 사용하는 것이 목적이기 때문에 like검색을 하자

```
package io.basquiat.boards.music.repository.impl;

import io.basquiat.boards.music.domain.entity.Label;
import io.basquiat.boards.music.repository.CustomLabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.CriteriaDefinition;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

@RequiredArgsConstructor
public class CustomLabelRepositoryImpl implements CustomLabelRepository {

    private final R2dbcEntityTemplate query;

    public Flux<Label> findLabelsWithPageable(Pageable pageable, String searchValue) {
        CriteriaDefinition criteriaWhere = null;
        if(StringUtils.hasLength(searchValue)) {
            criteriaWhere = where("name").like("%" + searchValue + "%");
        }
        return query.select(Label.class)
                    .matching(query(criteriaWhere).limit(pageable.getPageSize())
                                         .offset(pageable.getOffset())
                                         .sort(pageable.getSort()))
                    .all();

    }

}
```

# 쿼리 로깅의 불편함

```
logging:
  level:
    root: info
    io:
      basquiat:
        boards: debug
    org:
      springframework:
        r2dbc: DEBUG
```

일반적인 방식은 저렇게 하면 된다.

하지만 저렇게만 하면 정말 쿼리 로깅이 엄청 불친절하다. 파라미터 바인딩 자체도 볼 수 없다.

또한

```
logging.level.dev.miku.r2dbc.mysql.client.ReactorNettyClient=TRACE
```

이걸로 하면? Human-unreadable 스러운 로깅만 잔득 나온다.

차후에 좀더 나이스하게 로깅할 수 있기를 기원하며 다음과 같이 손을 좀 봐줘야 한다.

~~좀 패줘야...~~

그러기 위해서 다음을 그레이들에 추가하자

```
implementation 'io.r2dbc:r2dbc-proxy:0.8.8.RELEASE'
```
참고로 최신 버전은 0.9.0이지만 r2dbc-pool과 버전을 맞추고자 0.8.8.RELEASE로 세팅

그리고 리스너를 하나 만들어줘야 한다.


QueryLoggingListener.java
```
package io.basquiat.boards.common.listener;

import io.r2dbc.proxy.core.QueryExecutionInfo;
import io.r2dbc.proxy.listener.ProxyExecutionListener;
import io.r2dbc.proxy.support.QueryExecutionInfoFormatter;
import lombok.extern.slf4j.Slf4j;

/**
 * r2dbc proxy를 활용한 쿼리 로그
 */
@Slf4j
public class QueryLoggingListener implements ProxyExecutionListener {

    @Override
    public void afterQuery(QueryExecutionInfo execInfo) {
        QueryExecutionInfoFormatter formatter = new QueryExecutionInfoFormatter().addConsumer((info, sb) -> {
                                                                                        sb.append("ConnectionId: ");
                                                                                        sb.append(info.getConnectionInfo().getConnectionId());
                                                                                 })
                                                                                 .newLine()
                                                                                 .showQuery()
                                                                                 .newLine()
                                                                                 .showBindings()
                                                                                 .newLine();
        log.info(formatter.format(execInfo));
    }

}

```

r2dbc-proxy에 포함된 ProxyExecutionListener를 구현하는 방법이 공식 홈페이지에 있다.

일단 좀 더 세부적으로 할 수 있는거 같은데 공식 홈페이지의 예제를 따라가 본다.

또한 application.yml에도 변경을 해야 한다.

```
mysql:
  host: localhost
  port: 3306
  username: root
  password:
  database: basquiat
  proxy-listener: io.basquiat.boards.common.listener.QueryLoggingListener

spring:
  config:
    activate:
      on-profile: local
  r2dbc:
    url: r2dbc:proxy:mysql://${mysql.host}:${mysql.port}/${mysql.database}?proxyListener=${mysql.proxy-listener}&useSSL=false&useUnicode=yes&characterEncoding=UTF-8&serverTimezone=Asia/Seoul
    username: ${mysql.username}
    password: ${mysql.password}
    pool:
      enabled: true
      initial-size: 10
      max-size: 30
      max-idle-time: 30m
      validation-query: SELECT 1
```
url부분이 변경이 된 것을 확인해 보면 될것 같다.     

실제로 api를 호출하면 

```
[2022-02-11 17:59:23 KST] [INFO ] [reactor-tcp-nio-2] i.b.b.c.l.QueryLoggingListener:25 - ConnectionId: 1 
Query:["SELECT 1"] 
Bindings:[] 
 -- 
[2022-02-11 17:59:24 KST] [INFO ] [reactor-tcp-nio-2] i.b.b.c.l.QueryLoggingListener:25 - ConnectionId: 1 
Query:["SELECT label.* FROM label WHERE label.name LIKE ? ORDER BY label.id ASC LIMIT 5"] 
Bindings:[(%e%)] 
 -- 
```
과 같이 before/after로 쿼리 로깅이 된 것을 확인 할 수 있다.__

위에서 Result rows 정보도 찍을 수 있도록 작업완료

# At A Glance
뭔가 두서가 없는 코드들이 있어서 정리를 하고 로깅과 r2dbc-proxy를 통한 쿼리 로그 작업을 진행했다.

쿼리 로그의 경우에는 좀 더 공부를 해서 그럴싸한 로깅을 할 수 있도록 진행중에 조금씩 변경을 해보고자 한다.

일단 Label를 대상으로 간단한 페이징, 다이나믹 쿼리정도만 먼저 진행하고 다음 브랜치에서 좀 더 그럴싸하게 만들어 본다.     
