# 이전 브랜치 목록

[1.musician-section-1](https://github.com/basquiat78/boards-with-webflux/tree/1.musician-section-1)     

# 고전적인 방식으로 pageable
이전 브랜치에서 이 방법을 그냥 넘어갔는데 그렇다면 고전적인 방식으로 하면 어떻게 될까?

여기서 고전적인 방식이라는 것은 jpa와 같은 방식을 말하는 것으로 고전적인 방식이라는 말에 대한 오해를 넘어가고 자 한다.

다만 이럴 경우에는 확실히 말하자면 다이나믹한 쿼리 생성은 할 수 없다.

바로 시작해 보자.

일단 ReactiveCrudRepository은 Pageable을 받는 메소드가 없다.

따라서 다음과 같이 ReactiveSortingRepository로 변경하고 메소드를 하나 선언한다.

```
package io.basquiat.boards.music.repository;

import io.basquiat.boards.music.domain.entity.Label;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import reactor.core.publisher.Flux;

/**
 * label repository
 * created by basquiat
 */
public interface LabelRepository extends ReactiveSortingRepository<Label, Long>, CustomLabelRepository {

    Flux<Label> findAllBy(Pageable pageable);

}
```

서비스와 컨트롤러에도 필요한 메소드를 하나씩 추가하자.

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

    .
    .

    /**
     * pageable과 함께 음반사를 조회한다.
     * @return Flux<Musician>
     */
    public Flux<Label> fetchLabelsByWithPageable(SearchVO search) {
        Pageable pageable = PageRequest.of(search.getPage(), search.getSize(), sorting(search.getSort(), search.getOrder()));
        return labelRepository.findAllBy(pageable);
    }

    .
    .
}


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

    .
    .

    /**
     * Pageable을 적용해서 조회한다.
     * @return Flux<Label>
     */
    @GetMapping("/labels/pageable/by")
    @ResponseStatus(HttpStatus.OK)
    public Flux<Label> labelsByWithPageable(SearchVO searchVO) {
        return musicService.fetchLabelsByWithPageable(searchVO);
    }

}

```
이렇게 하면 원하는 결과를 얻을 수 있다.

하지만 좀더 세부적으로 pagenation에 대한 정보를 받을 수 있게 로직을 살짝 변경해 보자.

```
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
     * Pageable을 적용해서 조회한다.
     * @return Mono<Page<Label>>
     */
    @GetMapping("/labels/pageable/by")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Page<Label>> labelsByWithPageable(SearchVO searchVO) {
        return musicService.fetchLabelsByWithPageable(searchVO);
    }

```

결과는 다음과 같이

```
{
    "content": [
        {
            "id": 13,
            "name": "Sunnyside",
            "createdAt": "2022-02-10T14:08:21",
            "updatedAt": null
        },
        {
            "id": 12,
            "name": "Verve",
            "createdAt": "2022-02-10T14:06:26",
            "updatedAt": null
        },
        {
            "id": 11,
            "name": "MackAvenue",
            "createdAt": "2022-02-10T14:06:22",
            "updatedAt": null
        },
        {
            "id": 10,
            "name": "Fresh Sound New Talent",
            "createdAt": "2022-02-10T14:06:07",
            "updatedAt": null
        },
        {
            "id": 9,
            "name": "Edition Records",
            "createdAt": "2022-02-10T14:05:51",
            "updatedAt": null
        }
    ],
    "pageable": {
        "sort": {
            "unsorted": false,
            "sorted": true,
            "empty": false
        },
        "pageNumber": 0,
        "pageSize": 5,
        "offset": 0,
        "paged": true,
        "unpaged": false
    },
    "totalPages": 3,
    "totalElements": 13,
    "last": false,
    "numberOfElements": 5,
    "first": true,
    "number": 0,
    "sort": {
        "unsorted": false,
        "sorted": true,
        "empty": false
    },
    "size": 5,
    "empty": false
}
```
content라는 키에 조회된 정보가 나오고 페이징 처리 관련 정보가 잔득 들어오게 된다.

하지만 여기서는 저 방식은 사용하지 않을 예정이다. 페이징 처리를 위해서는 따로 작성을 할 예정이다.

사용하라고 만든 것들이기 때문에 다양한 선택지가 있다는 점에서 이 방법을 고려해본다면 충분히 사용하기에 부족함이 없을 것이다.

# 백오피스를 만든다고 한다면...

레이블 정보를 관리하는 메뉴, 뮤지션 정보를 관리하는 메뉴, 앨범 정보를 관리하는 메뉴들이 따로 존재하겠지만 현재 작성된 로직만으로는 무언가 부족하다.

예를 들면 어떤 뮤지션의 앨범 정보를 가져오겠다고 한다면 뮤지션 테이블과 앨범 테이블의 조인이 필수로 들어가게 될 것이고 앨범과 레이블의 조인이 들어가게 된다.

하지만 JPA와 다르기 때문에 이것을 해결하는 방법이 몇가지 존재하는데 이것을 소개하는 브랜치가 될것이다.

개발자의 입장에서는 어떤것이 더 좋은지는 확실히 개발 스타일/취향에 따른 선택지가 될것이므로 이 두가지를 다 소개하고자 한다.


## 1. 직접적으로 DatabaseClient를 활용해 쿼리를 작성하고 Mapper를 구현한다.

직접 쿼리를 짜는 방식을 먼저 생각해보자.

예를 들면 Album 정보를 가져오는 쿼리를 일단 만들어보자.

그 전에 나는 John Coltrane의 황금 쿼텟 era였던 Impulse! 레이블 시절의 작품들과 그의 유일한 블루노트 작품을 먼저 DB에 넣을 생각이기 때문에 일단 Label 테이블에 Impulse! 레이블 정보를 하나 생성하고 시작한다.

물론 API를 만들어서 사용해도 되고 쿼리로 직접 넣어도 상관없다.

```
//이넘은 블루노트 음반이네?
{
    "musicianId" : 1,
    "albumTitle" : "Blue Train",
    "albumRelease" : "1957",
    "genre" : "Hard Bop",
    "labelId" : 4
}

{
    "musicianId" : 1,
    "albumTitle" : "Live At The Village Vanguard",
    "albumRelease" : "1962",
    "genre" : "Hard Bop, Avant-garde",
    "labelId" : 14
}

{
    "musicianId" : 1,
    "albumTitle" : "Ballads",
    "albumRelease" : "1963",
    "genre" : "Hard Bop",
    "labelId" : 14
}

{
    "musicianId" : 1,
    "albumTitle" : "Impressions",
    "albumRelease" : "1963",
    "genre" : "Free Jazz, Post Bop",
    "labelId" : 14
}

{
    "musicianId" : 1,
    "albumTitle" : "A Love Supreme",
    "albumRelease" : "1965",
    "genre" : "Free Jazz, Hard Bop",
    "labelId" : 14
}

{
    "musicianId" : 1,
    "albumTitle" : "Ascension",
    "albumRelease" : "1966",
    "genre" : "Free Jazz, Post Bop",
    "labelId" : 14
}

{
    "musicianId" : 1,
    "albumTitle" : "Expression",
    "albumRelease" : "1967",
    "genre" : "Free Jazz, Post Bop",
    "labelId" : 14
}

```

특이사항.

현재 Album의 경우에는 엔티티를 만들고 save를 하게 되면 update쿼리가 나가면서 에러가 발생하는 것을 알 수 있다.

원래 앨범의 아이디를 UUID를 활용하기 위해서 사용했다가 jpa와는 다르게 반응한다는 것을 알 수있는데 이것을 해결하는 부분은 album테이블에서 varchar로 되어 있는 것을 UUID로 변경하고

Ablum entity를 수정해야 한다.

```
package io.basquiat.boards.music.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * album entity
 * created by basquiat
 */
@Builder
@Data
@Table("album")
@AllArgsConstructor
@RequiredArgsConstructor
public class Album implements Persistable<UUID> {

    /** unique id */
    @Id
    @Column("id")
    private UUID id;

    /** 뮤지션 아이디 */
    @Column("musician_id")
    private Long musicianId;

    /** 앨범 타이틀 */
    @Column("title")
    private String title;

    /** 앨범 발매 년도 e.g: yyyyMM */
    @Column("release")
    private String release;

    /** 앨범의 음악 장르 */
    @Column("genre")
    private String genre;

    /** 앨범이 발매된 레이블 아이디 */
    @Column("label_id")
    private Long labelId;

    /** 등록일 */
    @Column("created_at")
    private LocalDateTime createdAt;

    /** 갱신일 */
    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean isNew() {
        boolean result = Objects.isNull(id);
        this.id = result ? UUID.randomUUID() : this.id;
        return result;
    }

}

```

하지만 테이블을 변경하고 싶지 않다면 다음과 같이 CustomAlbumRepository를 생성할 수 밖에 없다.

그 전에 분명 문제가 없는 코드라 생각했는데 오류가 나서 테스트하다 알게 된 사실인데 release라는 말이 mySql에서 예약어란다....

장르 컬럼도 좀 크게 하자.

```
ALTER TABLE basquiat.album CHANGE release release_year varchar(4) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '앨범 발매 년도 e.g: yyyy';
ALTER TABLE basquiat.album MODIFY COLUMN genre varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '앨범의 음악 장르';


package io.basquiat.boards.music.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * album entity
 * created by basquiat
 */
@Builder
@Data
@Table("album")
@AllArgsConstructor
@RequiredArgsConstructor
public class Album {

    /** unique id */
    @Id
    @Column("id")
    private String id;

    /** 뮤지션 아이디 */
    @Column("musician_id")
    private Long musicianId;

    /** 앨범 타이틀 */
    @Column("title")
    private String title;

    /** 앨범 발매 년도 e.g: yyyy */
    @Column("release_year")
    private String releaseYear;

    /** 앨범의 음악 장르 */
    @Column("genre")
    private String genre;

    /** 앨범이 발매된 레이블 아이디 */
    @Column("label_id")
    private Long labelId;

    /** 등록일 */
    @Column("created_at")
    private LocalDateTime createdAt;

    /** 갱신일 */
    @Column("updated_at")
    private LocalDateTime updatedAt;

}

```

살짝 바꿔주고 다음과 같이

```
package io.basquiat.boards.music.repository;

import io.basquiat.boards.music.domain.entity.Album;
import reactor.core.publisher.Mono;

public interface CustomAlbumRepository {

    Mono<Album> insertAlbum(Album album);

}

package io.basquiat.boards.music.repository.custom.impl;

import io.basquiat.boards.music.domain.entity.Album;
import io.basquiat.boards.music.repository.custom.CustomAlbumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CustomAlbumRepositoryImpl implements CustomAlbumRepository {

    private final R2dbcEntityTemplate query;

    @Override
    public Mono<Album> insertAlbum(Album album) {
        return query.insert(album);
    }

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

```
처럼 명시적으로 그냥 인서트를 하게 만들자.

어찌되었든 album테이블의 어느정도 데이터를 밀어넣었다면 이제부터 조인을 해보자.

JPA처럼 foreign key를 가장 많이 가지고 있는 Albums를 Aggregation root로 생각을 해보자.

album을 통해서 뮤지션/레이블 정보를 다 조회할 수 있기 때문이라는 것은 더이상 언급하지 않겠다.

당연 여기에 기저에 깔려 있는 것은 쿼리가 날아갔을 때 결과값에 대한 그림이 그려져야 한다.

즉, 쿼리를 잘 알아야 하고 결과에 대한 예상을 할 줄 알아야 한다.

Album과 Muscian 또는 Musician과 Album의 관계는 JPA가 익숙하다면 그려져야 한다.

음악가 입장에서는 여러장의 앨범을 가질수 있으니 OneToMany 관계가 된다는 것, 그리고 앨범 입장에서는 그 반대의 ManyToOne이라는 것을 알 수 있다.

앨범과 레이블은 ManyToOne이라는 것을 알 수 있다.

자 그러면 다음과 같은 쿼리를 날려본다고 생각을 해보자.


```
-- album table을 기준으로 musician과 테이블은 inner join으로
SELECT a.title AS albumTitle,
       m.name AS musicianName,
       l.name AS labelName
	FROM album a
	JOIN musician m ON a.musician_id = m.id
	JOIN label l ON a.label_id = l.id
   WHERE m.id = 1;

 -- musician table을 기준으로 album은 없을 수도 있으므로 left join, 
 -- 앨범 정보를 inner join으로 하면 앨범 정보가 없으면 뮤지션 정보도 조회되지 않는다.     
 SELECT a.title AS albumTitle,
       m.name AS musicianName,
       l.name AS labelName
	FROM musician m
	LEFT JOIN album a ON m.id = a.musician_id
	JOIN label l ON a.label_id = l.id
   WHERE m.id = 1;
```
조회할때 Projection정보가 몇개 없지만 쿼리의 모양을 보면 결과를 리턴받은 이후에 musician의 이름이나 아이디로 그룹핑을 할 수 있다는 것을 알 수 있다.

mapper나 코드레벨에서 결국 이 관계를 객체로 다루고자 한다면 결과를 받은 이후 일종의 그룹핑을 한다는 것을 알 수 있게 된다.

하지만 R2DBC는 JPA같은 ORM이 아니기 때문에 prepareStatement나 jdbcTemplate같은 방식으로 resultSet를 직접적으로 다루는 Old School스타일로 코딩할 수 밖에 없다.

이제 코드로 직접 보자.

CustomAlbumRepository.java에 메소드를 하나 선언해 보자.
```
    Flux<Album> findAlbumByMusicianId(Long id);
```


CustomAlbumRepositoryImpl에서 구현를 해보자.
```

    @Override
    public Flux<Album> findAlbumByMusicianId(Long id) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT a.*, ");
        sb.append("       m.id AS musicianId, ");
        sb.append("       m.name AS musicianName, ");
        sb.append("       m.instrument, ");
        sb.append("       m.birth, ");
        sb.append("       l.id AS labelId, ");
        sb.append("       l.name AS labelName ");
        sb.append("     FROM album a");
        sb.append("     JOIN musician m ON a.musician_id = m.id");
        sb.append("     JOIN label l ON a.label_id = l.id");
        sb.append("    WHERE m.id = :id ");

        return query.getDatabaseClient().sql(sb.toString())
                                        .bind("id", id)
                                        .map(row -> Album.builder()
                                                         .id(row.get("id").toString())
                                                         .musicianId(row.get("musicianId", Long.class))
                                                         .title(row.get("title").toString())
                                                         .releaseYear(row.get("release_year").toString())
                                                         .genre(row.get("genre").toString())
                                                         .labelId(row.get("labelId", Long.class))
                                                         .createdAt(row.get("created_at", LocalDateTime.class))
                                                         .updatedAt(row.get("updated_at", LocalDateTime.class))
                                                         .musician(Musician.builder()
                                                                           .id(row.get("musicianId", Long.class))
                                                                           .name(row.get("musicianName").toString())
                                                                           .instrument(row.get("instrument").toString())
                                                                           .birth(row.get("birth").toString())
                                                                           .build())
                                                         .label(Label.builder()
                                                                     .id(row.get("labelId", Long.class))
                                                                     .name(row.get("labelName").toString())
                                                                     .build())
                                                         .build())
                                        .all();
    }

}

```
위에서 보면 알겠지만 map에 대한 정보를 따라가면 Function과 BiFunction 두개를 받을 수 있게 되어 있다.

직접적으로 저렇게 코드를 짜겠다면 애초에 Result type을 반환하게 되어 있어서 그냥 사용하면 된다.

하지만 저렇게 코드를 작성하게 되면 차후 수정을 할때 참... 지랄맞는 코드가 아닐 수 없다.

가장 손쉬운 방법은 그냥 fuctional interface사용이다.

```
@Override
    public Flux<Album> findAlbumByMusicianId(Long id, Pageable pageable) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT a.*, ");
        sb.append("       m.id AS musicianId, ");
        sb.append("       m.name AS musicianName, ");
        sb.append("       m.instrument, ");
        sb.append("       m.birth, ");
        sb.append("       l.id AS labelId, ");
        sb.append("       l.name AS labelName ");
        sb.append("     FROM album a");
        sb.append("     JOIN musician m ON a.musician_id = m.id");
        sb.append("     JOIN label l ON a.label_id = l.id");
        sb.append("    WHERE m.id = :id ");
        sb.append("    LIMIT :limit OFFSET :offset ");

        Function<Row, Album> expression =  (r) -> Album.builder()
                                                       .id(r.get("id").toString())
                                                       .musicianId(r.get("musicianId", Long.class))
                                                       .title(r.get("title").toString())
                                                       .releaseYear(r.get("release_year").toString())
                                                       .genre(r.get("genre").toString())
                                                       .labelId(r.get("labelId", Long.class))
                                                       .createdAt(r.get("created_at", LocalDateTime.class))
                                                       .updatedAt(r.get("updated_at", LocalDateTime.class))
                                                       .musician(Musician.builder()
                                                                         .id(r.get("musicianId", Long.class))
                                                                         .name(r.get("musicianName").toString())
                                                                         .instrument(r.get("instrument").toString())
                                                                         .birth(r.get("birth").toString())
                                                                         .build())
                                                       .label(Label.builder()
                                                                   .id(r.get("labelId", Long.class))
                                                                   .name(r.get("labelName").toString())
                                                                   .build())
                                                       .build();
        return query.getDatabaseClient().sql(sb.toString())
                                        .bind("id", id)
                                        .bind("limit", pageable.getPageSize())
                                        .bind("offset", pageable.getOffset())
                                        .map(row -> expression.apply(row))
                                        .all();
    }
```

또는 private 메소드를 만들어서 사용하는 것이다.

```
@Override
    public Flux<Album> findAlbumByMusicianId(Long id, Pageable pageable) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT a.*, ");
        sb.append("       m.id AS musicianId, ");
        sb.append("       m.name AS musicianName, ");
        sb.append("       m.instrument, ");
        sb.append("       m.birth, ");
        sb.append("       l.id AS labelId, ");
        sb.append("       l.name AS labelName ");
        sb.append("     FROM album a");
        sb.append("     JOIN musician m ON a.musician_id = m.id");
        sb.append("     JOIN label l ON a.label_id = l.id");
        sb.append("    WHERE m.id = :id ");
        sb.append("    LIMIT :limit OFFSET :offset ");
        return query.getDatabaseClient().sql(sb.toString())
                                        .bind("id", id)
                                        .bind("limit", pageable.getPageSize())
                                        .bind("offset", pageable.getOffset())
                                        .map(this::mapper)
                                        .all();
    }

    private Album mapper(Row row) {
        Function<Row, Album> expression =  (r) -> Album.builder()
                                                       .id(r.get("id").toString())
                                                       .musicianId(r.get("musicianId", Long.class))
                                                       .title(r.get("title").toString())
                                                       .releaseYear(r.get("release_year").toString())
                                                       .genre(r.get("genre").toString())
                                                       .labelId(r.get("labelId", Long.class))
                                                       .createdAt(r.get("created_at", LocalDateTime.class))
                                                       .updatedAt(r.get("updated_at", LocalDateTime.class))
                                                       .musician(Musician.builder()
                                                                         .id(r.get("musicianId", Long.class))
                                                                         .name(r.get("musicianName").toString())
                                                                         .instrument(r.get("instrument").toString())
                                                                         .birth(r.get("birth").toString())
                                                                         .build())
                                                       .label(Label.builder()
                                                                   .id(r.get("labelId", Long.class))
                                                                   .name(r.get("labelName").toString())
                                                                   .build())
                                                       .build();
        return expression.apply(row);
    }

```

나는 레파지토리에 저런 코드가 들어가는게 싫다. 그렇다면 Mapper를 작성해 보자.

Mapper를 작성할 때는 functional interface를 구현하면 된다.

```
package io.basquiat.boards.music.mapper;

import io.basquiat.boards.music.domain.entity.Album;
import io.basquiat.boards.music.domain.entity.Label;
import io.basquiat.boards.music.domain.entity.Musician;
import io.r2dbc.spi.Row;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.function.Function;

/**
 * album mapper
 */
@Component
public class AlbumMapper implements Function<Row, Album> {

    @Override
    public Album apply(Row row) {
        return Album.builder()
                    .id(row.get("id").toString())
                    .musicianId(row.get("musicianId", Long.class))
                    .title(row.get("title").toString())
                    .releaseYear(row.get("release_year").toString())
                    .genre(row.get("genre").toString())
                    .labelId(row.get("labelId", Long.class))
                    .createdAt(row.get("created_at", LocalDateTime.class))
                    .updatedAt(row.get("updated_at", LocalDateTime.class))
                    .musician(Musician.builder()
                                      .id(row.get("musicianId", Long.class))
                                      .name(row.get("musicianName").toString())
                                      .instrument(row.get("instrument").toString())
                                      .birth(row.get("birth").toString())
                                      .build())
                    .label(Label.builder()
                                .id(row.get("labelId", Long.class))
                                .name(row.get("labelName").toString())
                                .build())
                    .build();
    }

}

    @Override
    public Flux<Album> findAlbumByMusicianId(Long id) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT a.*, ");
        sb.append("       m.id AS musicianId, ");
        sb.append("       m.name AS musicianName, ");
        sb.append("       m.instrument, ");
        sb.append("       m.birth, ");
        sb.append("       l.id AS labelId, ");
        sb.append("       l.name AS labelName ");
        sb.append("     FROM album a");
        sb.append("     JOIN musician m ON a.musician_id = m.id");
        sb.append("     JOIN label l ON a.label_id = l.id");
        sb.append("    WHERE m.id = :id ");
        return query.getDatabaseClient().sql(sb.toString())
                                        .bind("id", id)
                                        .map(albumMapper::apply)
                                        .all();
    }

```
매퍼로 처리하면 로직이 깔끔해 진다.

자 이렇다는 것은 엔티티로 매핑해서 반환해도 좋고 매퍼라는 기능에 충실하게 사용해서 DTO로 반환해도 충분할 것이다.

참고로 all()메소드를 기점으로 이전의 map과 이 후의 map은 모양새가 좀 다르다는 것을 알 수 있다.

all()이 이전에는 DatabaseClient에서 제공하는 map이며 이 후에는 WebFlux의 Flux에서 제공하는 map이다.

사실 그에 맞춘다면 해당 메소드에서 제공하는 시그니처를 따라가는게 맞지만 여기서는 간략하게 필요한 것만 사용해 코드를 구현한다.

## 2. JPA스럽게 converter를 이용하면 어떨까?

기존에 삭제했던 Configuration을 살려야한다. 이 방법은 좀 거시기 한게 공식 오피셜대로 하면 안된다....

일단 AlbumConverter를 만들어 보자.

```
package io.basquiat.boards.music.converter;

import io.basquiat.boards.music.domain.entity.Album;
import io.basquiat.boards.music.domain.entity.Label;
import io.basquiat.boards.music.domain.entity.Musician;
import io.r2dbc.spi.Row;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.time.LocalDateTime;

@ReadingConverter
public class AlbumConverter implements Converter<Row, Album> {

    @Override
    public Album convert(Row row) {
        return Album.builder()
                    .id(row.get("id").toString())
                    .musicianId(row.get("musicianId", Long.class))
                    .title(row.get("title").toString())
                    .releaseYear(row.get("release_year").toString())
                    .genre(row.get("genre").toString())
                    .labelId(row.get("labelId", Long.class))
                    .createdAt(row.get("created_at", LocalDateTime.class))
                    .updatedAt(row.get("updated_at", LocalDateTime.class))
                    .musician(Musician.builder()
                                      .id(row.get("musicianId", Long.class))
                                      .name(row.get("musicianName").toString())
                                      .instrument(row.get("instrument").toString())
                                      .birth(row.get("birth").toString())
                                      .build())
                    .label(Label.builder()
                                .id(row.get("labelId", Long.class))
                                .name(row.get("labelName").toString())
                                .build())
                    .build();
    }

}

```
관점에서 볼때 넘겨받은 엔티티인 Album과 관련해서 해당 레파지토리에서 어떤 이벤트가 발생하면 껴들어가는 형식인듯 싶다.

근데 @ReadingConverter이라는 어노테이션명에서도 알 수 있듯이 셀렉트 이후 무언가를 하는 넘이라는것을 알 수 있다.

그렇다는 것은? db로 업데이트나 인서트를 할때 무언가를 할 수 있다는 사실을 알 수 있다.

그리고 살린 configuration에서 해당 컨버터를 등록해줘야 한다.

그렇다는 것은 하나씩 늘어날때마다 이것을 추가해줘야 하는 고통을 당해야 한다는 것인가???

차라리 JPA처럼 해당 메소드에 어노테이션을 통해서 컨버트유무를 설정하고 해당 메소드가 실행될때 작동하게 만들면 안될까???????

암튼 일단 찾아보고는 있는데 딱히 예제가 없어서..

```
package io.basquiat.boards.common.configuration;


import io.basquiat.boards.music.converter.AlbumConverter;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableR2dbcRepositories
public class R2dbcConfiguration extends AbstractR2dbcConfiguration {

    @Override
    public ConnectionFactory connectionFactory() {
        return null;
    }

    @Override
    protected List<Object> getCustomConverters() {
        List<Object> converterList = new ArrayList<>(); // 공식 홈페이지는 Converter<?, ?>로 되어 있는데 그냥 Object로....
        converterList.add(new AlbumConverter());
        return converterList;
    }

}

```

AlbumRepository.java
```
package io.basquiat.boards.music.repository;

import io.basquiat.boards.music.domain.entity.Album;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

/**
 * album repository
 * created by basquiat
 */
public interface AlbumRepository extends ReactiveCrudRepository<Album, String>, CustomAlbumRepository {

    @Query("SELECT a.*, m.id AS musicianId, m.name AS musicianName, m.instrument, m.birth, l.id AS labelId, l.name AS labelName FROM album a JOIN musician m ON a.musician_id = m.id JOIN label l ON a.label_id = l.id WHERE m.id = :id LIMIT :limit OFFSET :offset")
    Flux<Album> findAll(@Param("id") Long id, @Param("limit") int limit, @Param("offset") Long offset);

}

```
저렇게 괴랄한.... 넘을 만들어주고

서비스 쪽에 저 넘을 호출하는 녀석을 호출해 보자.
```
    /**
     * album 정보를 가져온다.
     * @param musicianId
     * @param search
     * @return Flux<Album>
     */
    public Flux<Album> fetchAlbumsByMusicianCovert(Long musicianId, SearchVO search) {
        Pageable pageable = PageRequest.of(search.getPage(), search.getSize(), sorting(search.getSort(), search.getOrder()));
        return albumRepository.findAll(musicianId, pageable.getPageSize(), pageable.getOffset());
    }
```

결과는

```
[
    {
        "id": "0cf84d83-2aab-42de-be50-b20d1f424306",
        "musicianId": 1,
        "title": "Blue Train",
        "releaseYear": "1957",
        "genre": "Hard Bop",
        "labelId": 4,
        "createdAt": "2022-02-15T15:38:40",
        "updatedAt": null,
        "musician": {
            "id": 1,
            "name": "John Coltrane",
            "instrument": "Tenor Saxophone, Soprano Saxophone, Bass Clarinet",
            "birth": "19260923",
            "createdAt": null,
            "updatedAt": null
        },
        "label": {
            "id": 4,
            "name": "BlueNote",
            "createdAt": null,
            "updatedAt": null
        }
    },
    {
        "id": "108b2647-6c8a-4420-904d-94e0b95cc6bf",
        "musicianId": 1,
        "title": "1963: New Directions",
        "releaseYear": "2018",
        "genre": "Free Jazz, Post Bop",
        "labelId": 14,
        "createdAt": "2022-02-15T16:57:35",
        "updatedAt": null,
        "musician": {
            "id": 1,
            "name": "John Coltrane",
            "instrument": "Tenor Saxophone, Soprano Saxophone, Bass Clarinet",
            "birth": "19260923",
            "createdAt": null,
            "updatedAt": null
        },
        "label": {
            "id": 14,
            "name": "Impulse!",
            "createdAt": null,
            "updatedAt": null
        }
    },
    {
        "id": "207a8f79-8130-424f-9518-c8f36ba28a6e",
        "musicianId": 1,
        "title": "Expression",
        "releaseYear": "1967",
        "genre": "Free Jazz, Post Bop",
        "labelId": 14,
        "createdAt": "2022-02-15T15:49:19",
        "updatedAt": null,
        "musician": {
            "id": 1,
            "name": "John Coltrane",
            "instrument": "Tenor Saxophone, Soprano Saxophone, Bass Clarinet",
            "birth": "19260923",
            "createdAt": null,
            "updatedAt": null
        },
        "label": {
            "id": 14,
            "name": "Impulse!",
            "createdAt": null,
            "updatedAt": null
        }
    },
    {
        "id": "3804035f-6b98-42f0-ba76-35a1cb0dccb6",
        "musicianId": 1,
        "title": "A Love Supreme: Live In Seattle",
        "releaseYear": "2021",
        "genre": "Free Jazz, Post Bop",
        "labelId": 14,
        "createdAt": "2022-02-15T16:55:55",
        "updatedAt": null,
        "musician": {
            "id": 1,
            "name": "John Coltrane",
            "instrument": "Tenor Saxophone, Soprano Saxophone, Bass Clarinet",
            "birth": "19260923",
            "createdAt": null,
            "updatedAt": null
        },
        "label": {
            "id": 14,
            "name": "Impulse!",
            "createdAt": null,
            "updatedAt": null
        }
    },
    {
        "id": "39d209d7-7d86-456e-bf8f-0ef2e7da3c65",
        "musicianId": 1,
        "title": "Ballads",
        "releaseYear": "1963",
        "genre": "Hard Bop",
        "labelId": 14,
        "createdAt": "2022-02-15T15:48:55",
        "updatedAt": null,
        "musician": {
            "id": 1,
            "name": "John Coltrane",
            "instrument": "Tenor Saxophone, Soprano Saxophone, Bass Clarinet",
            "birth": "19260923",
            "createdAt": null,
            "updatedAt": null
        },
        "label": {
            "id": 14,
            "name": "Impulse!",
            "createdAt": null,
            "updatedAt": null
        }
    }
]
```
처럼 mapper를 사용했을 때와 같이 똑같은 결과를 얻을 수 있다.

일단 나는 converter는 잘 사용하지 않을거 같다.

뭐 차후에 더 좋아지고 JPA처럼 어노테이션 설정만으로 적용할 수 있는 방법이 등장한다면 고려해보겠지만.....

여러분은 어떤게 더 나은거 같은가?

자 그럼 이제는 JPA의 양방향 설정처럼 musician table을 기준으로 하는 OneToMany를 어떻게 적용할 것인지 해봐야한다.

일단 테스트를 하다보니 null인 녀석들이 보기 싫어서 모든 엔티티에 @JsonInclude(JsonInclude.Include.NON_NULL)을 달아주자.

OneToMany는 사실 고려해야 할 부분이 많다.

실제로 나는 JPA에서도 그렇고 webflux에서는 이 관계의 경우에는 좀 다른 방식으로 풀어간다.

어떤 특정 뮤지션아이디에 해당하는 경우라면 다르지만 만일 그냥 뮤지션을 조건없이 조회하는 경우에는 이 album정보를 담는 방식이 문제가 발생할 수 있다고 보기 때문이다.

특히 페이징 부분.

그래서 여기서는 특정 뮤지션에 대한 예제만 할 예정이다.

쿼리는 위에 적어논 쿼리를 참조하면

```
 SELECT a.title AS albumTitle,
        m.name AS musicianName,
        l.name AS labelName
	FROM musician m
	LEFT JOIN album a ON m.id = a.musician_id
	JOIN label l ON a.label_id = l.id
   WHERE m.id = 1;
```
여기에서 확장을 해보면

```
SELECT m.*,
        a.id AS albumId,
        a.title,
        a.release_year,
        a.genre,
        l.id AS labelId,
        l.name AS labelName
	FROM musician m
	LEFT JOIN album a ON m.id = a.musician_id
	JOIN label l ON a.label_id = l.id
   WHERE m.id = 1;
```
이런 식의 쿼리가 될 것이다.

```
package io.basquiat.boards.music.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.List;

/**
 * musician entity
 * created by basquiat
 */
@Builder
@Data
@Table("musician")
@AllArgsConstructor
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Musician {

    /** unique id */
    @Id
    @Column("id")
    private Long id;

    /** 뮤지션 이름 */
    @Column("name")
    private String name;

    /** 뮤지션이 다루는 악기 정보 */
    @Column("instrument")
    private String instrument;

    /** 뮤지션의 생년월일 e.g: yyyyMMdd */
    @Column("birth")
    private String birth;

    /** 등록일 */
    @Column("created_at")
    private LocalDateTime createdAt;

    /** 갱신일 */
    @Column("updated_at")
    private LocalDateTime updatedAt;

    private List<Album> albums;

}
```
album 정보를 담아야 하니 위와 같이 하나를 추가한다.


CustomMusicianRepository.java
```
package io.basquiat.boards.music.repository;

import io.basquiat.boards.music.domain.entity.Musician;
import reactor.core.publisher.Mono;

public interface CustomMusicianRepository {

    Mono<Musician> findMusicianById(Long id);

}

```
아이디로 검색하게 메소드 하나 만들고

CustomMusicianRepositoryImpl.java
```
package io.basquiat.boards.music.repository.custom.impl;

import io.basquiat.boards.music.domain.entity.Musician;
import io.basquiat.boards.music.repository.custom.CustomMusicianRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import reactor.core.publisher.Mono;

import static io.basquiat.boards.common.utils.CommonUtils.toJson;

@Slf4j
@RequiredArgsConstructor
public class CustomMusicianRepositoryImpl implements CustomMusicianRepository {

    private final R2dbcEntityTemplate query;

    @Override
    public Mono<Musician> findMusicianById(Long id) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT musician.id AS 01_id, ");
        sb.append("       musician.name AS 02_name, ");
        sb.append("       musician.instrument AS 03_instrument, ");
        sb.append("       musician.birth AS 04_birth, ");
        sb.append("       musician.created_at AS 05_created, ");
        sb.append("       musician.updated_at AS 06_updated, ");
        sb.append("       album.id AS 07_albumId, ");
        sb.append("       album.title AS 08_title, ");
        sb.append("       album.release_year AS 09_releaseYear, ");
        sb.append("       album.genre AS 10_genre, ");
        sb.append("       label.id AS 11_labelId, ");
        sb.append("       label.name AS 12_labelName ");
        sb.append("     FROM musician");
        sb.append("     LEFT JOIN album ON musician.id = album.musician_id");
        sb.append("     JOIN label ON album.label_id = label.id");
        sb.append("    WHERE musician.id = :id ");

        return query.getDatabaseClient().sql(sb.toString())
                                        .bind("id", id)
                                        .fetch()
                                        .all()
                                        .bufferUntilChanged(result -> result.get("01_id"))
                                        .map(rows -> {
                                            return Musician.builder().name(rows.get(0).get("02_name").toString()).build();
                                        })
                                        .take(1)
                                        .next();
    }
}

```
위처럼 처리를 하자.

일단 위에 AS 뒤에 요상하게 별칭을 준 것을 볼 수 있는데 이 로직이 수행되면 이상하게도 저 형태의 정보를 조회하고 선언된 컬럼을 asc로 소팅해서 Map객체로 변환한다.

실제로 fetch()가 실행되면서 Row객체를 Map객체로 변환하면서 이런 현상이 발생이 된다.

메소드를 따라가다보면 최종적으로 RowMetadata의 columnNames정보를 가져와 루프를 돌면서 생성하게 되는데 MySqlRowMetadata객체를 들어가면     
name sort를 하는 듯한 코드가 눈에 띈다.

그래서 아마도 이런 이유로 몇 몇 예제를 보면 컬럼이 이상하게 매핑된다는 이야기를 보게 되는데 이것때문인거 같다.

어째든....

```
sb.append("SELECT musician.id AS 01_id, ");
sb.append("       musician.name AS 02_name, ");
sb.append("       musician.instrument AS 03_instrument, ");
sb.append("       musician.birth AS 04_birth, ");
sb.append("       musician.created_at AS 05_created, ");
sb.append("       musician.updated_at AS 06_updated, ");
sb.append("       album.id AS 07_albumId, ");
sb.append("       album.title AS 08_title, ");
sb.append("       album.release_year AS 09_releaseYear, ");
sb.append("       album.genre AS 10_genre, ");
sb.append("       label.id AS 11_labelId, ");
sb.append("       label.name AS 12_labelName ");
```
저 코드에서 prefix로 달아논 넘버를 제거하면

실제 쿼리를 하나 날려보면
```
musicianId|name         |instrument                                       |birth   |albumId                             |release_year|genre                     |labelId|labelName|
----------+-------------+-------------------------------------------------+--------+------------------------------------+------------+--------------------------+-------+---------+
         1|John Coltrane|Tenor Saxophone, Soprano Saxophone, Bass Clarinet|19260923|0cf84d83-2aab-42de-be50-b20d1f424306|1957        |Hard Bop                  |      4|BlueNote |
         1|John Coltrane|Tenor Saxophone, Soprano Saxophone, Bass Clarinet|19260923|108b2647-6c8a-4420-904d-94e0b95cc6bf|2018        |Free Jazz, Post Bop       |     14|Impulse! |
         1|John Coltrane|Tenor Saxophone, Soprano Saxophone, Bass Clarinet|19260923|207a8f79-8130-424f-9518-c8f36ba28a6e|1967        |Free Jazz, Post Bop       |     14|Impulse! |
         1|John Coltrane|Tenor Saxophone, Soprano Saxophone, Bass Clarinet|19260923|3804035f-6b98-42f0-ba76-35a1cb0dccb6|2021        |Free Jazz, Post Bop       |     14|Impulse! |
         1|John Coltrane|Tenor Saxophone, Soprano Saxophone, Bass Clarinet|19260923|39d209d7-7d86-456e-bf8f-0ef2e7da3c65|1963        |Hard Bop                  |     14|Impulse! |
         1|John Coltrane|Tenor Saxophone, Soprano Saxophone, Bass Clarinet|19260923|93198d27-7770-4409-a91a-a1291b6aa2ed|1962        |Hard Bop, Avant-garde Jazz|     14|Impulse! |
         1|John Coltrane|Tenor Saxophone, Soprano Saxophone, Bass Clarinet|19260923|948dc519-9601-4e3c-af3c-5ed7e7090a27|1963        |Free Jazz, Post Bop       |     14|Impulse! |
         1|John Coltrane|Tenor Saxophone, Soprano Saxophone, Bass Clarinet|19260923|aca305cd-ffde-44d0-b8aa-a4a251e5c2e3|1965        |Free Jazz, Hard Bop       |     14|Impulse! |
         1|John Coltrane|Tenor Saxophone, Soprano Saxophone, Bass Clarinet|19260923|bdf631b8-3655-4e49-95d8-a6b1a629194a|1966        |Free Jazz, Post Bop       |     14|Impulse! |
```
위와 같이 가져와서 저 정보를 위에 설정한 별칭되로 소팅을 한 이후 Map객체에 담는 요상한 짓을 한다.

수많은 자료를 찾아서 작성한 코드이기에 내가 무엇을 놓쳤는지 모르지만 실제로 쿼리 이후 result row 로깅을 보면

```
[reactor-tcp-nio-2] i.b.b.c.l.QueryLoggingListener:38 - Result Row : {albumId=1, birth=John Coltrane, genre=Tenor Saxophone, Soprano Saxophone, Bass Clarinet, ....} -- 
```
저렇게 매핑되서 결과가 나오게 된다.

그래서 저렇게 순차적인 소팅이 되도록 하게 만들면 ....

```
Result Row : {01_id=1, 02_name=John Coltrane, 03_instrument=Tenor Saxophone, Soprano Saxophone, Bass Clarinet, 04_birth=19260923, 05_created=2022-02-10T14:24:43, 06_updated=null, 07_albumId=0cf84d83-2aab-42de-be50-b20d1f424306, 08_title=Blue Train, 09_releaseYear=1957, 10_genre=Hard Bop, 11_labelId=4, 12_labelName=BlueNote} --
```
위와 같이 정확하게 매핑을 시킬 수 있다.

내부 코드를 찾아봐도 지식이 딸려서....

암튼 저런 짓거리를 해야 한다는 것이 이해할 수 없지만 그래도 정확하게 정보를 가져와야 하니.... ㅠㅠㅠㅠ


아무튼 코드에서

```
.bind("id", id)
.fetch()
.all()
```
을 하게 되면 우리가 알고 있는 저 각각의 row 정보들을 Flux형태로 존재할텐데 여기서

```
.bufferUntilChanged(result -> result.get("01_id"))
```
을 하게 되면 일종의 그룹핑처럼 생성된다.

Flux<Map<String, Object>의 형식이 Flux<List<Map<String, Object>>의 형식으로 변환된다.

그 이후 Map을 통해서 musician_id로 매핑된 리스트 정보들을 받아오게 된다.

예를 들면 지금은 musician_id가 1인 경우라서 그렇지 만일 조건이 없거나 IN으로 조회하게 되면 여러명의 뮤지션과 그 뮤지션의 앨범 숫자만큼 Row가 증가될 텐데

저 코드를 통해서 뮤지션 아이디를 키로 한 리스트 정보를 묶어서 보내게 되는 것이다.

그렇다면

```
.map(rows -> {
    return Musician.builder().name(rows.get(0).get("02_name").toString()).build();
})
.take(1)
.next();
```

여기서 rows를 통해서 Musician객체를 생성해 반환하게 될 것이고

실제로 저 쿼리는 한명의 뮤지션이 나올테니 take(1)을 통해 하나만 가져와서 next()를 통해 Mono<Musician>으로 반환하는 코드가 된다.

완성된 코드

```
package io.basquiat.boards.music.repository.custom.impl;

import io.basquiat.boards.music.domain.entity.Album;
import io.basquiat.boards.music.domain.entity.Label;
import io.basquiat.boards.music.domain.entity.Musician;
import io.basquiat.boards.music.repository.custom.CustomMusicianRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.mapping.Column;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

import static io.basquiat.boards.common.utils.CommonUtils.toJson;
import static io.basquiat.boards.common.utils.DateUtils.toDateTime;
import static io.basquiat.boards.common.utils.NumberUtils.parseLong;
import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
public class CustomMusicianRepositoryImpl implements CustomMusicianRepository {

    private final R2dbcEntityTemplate query;

    @Override
    public Mono<Musician> findMusicianById(Long id) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT musician.id AS 01_id, ");
        sb.append("       musician.name AS 02_name, ");
        sb.append("       musician.instrument AS 03_instrument, ");
        sb.append("       musician.birth AS 04_birth, ");
        sb.append("       musician.created_at AS 05_created, ");
        sb.append("       musician.updated_at AS 06_updated, ");
        sb.append("       album.id AS 07_albumId, ");
        sb.append("       album.title AS 08_title, ");
        sb.append("       album.release_year AS 09_releaseYear, ");
        sb.append("       album.genre AS 10_genre, ");
        sb.append("       label.id AS 11_labelId, ");
        sb.append("       label.name AS 12_labelName ");
        sb.append("     FROM musician");
        sb.append("     LEFT JOIN album ON musician.id = album.musician_id");
        sb.append("     JOIN label ON album.label_id = label.id");
        sb.append("    WHERE musician.id = :id ");

        return query.getDatabaseClient().sql(sb.toString())
                                        .bind("id", id)
                                        .fetch()
                                        .all()
                                        .bufferUntilChanged(result -> result.get("01_id"))
                                        .map(rows ->
                                                Musician.builder()
                                                        .id(parseLong(rows.get(0).get("01_id")))
                                                        .name(rows.get(0).get("02_name").toString())
                                                        .instrument(rows.get(0).get("03_instrument").toString())
                                                        .birth(rows.get(0).get("04_birth").toString())
                                                        .createdAt(toDateTime(rows.get(0).get("05_created")))
                                                        .updatedAt(toDateTime(rows.get(0).get("06_updated")))
                                                        .albums(rows.stream()
                                                                    .map(row -> Album.builder()
                                                                            .id(row.get("07_albumId").toString())
                                                                            .title(row.get("08_title").toString())
                                                                            .releaseYear(row.get("09_releaseYear").toString())
                                                                            .genre(row.get("10_genre").toString())
                                                                            .label(Label.builder()
                                                                                        .id(parseLong(row.get("11_labelId")))
                                                                                        .name(row.get("12_labelName").toString())
                                                                                        .build())
                                                                            .build())
                                                                    .collect(toList()))
                                                        .build()
                                        )
                                        .take(1)
                                        .next();
    }

}

```

~~어휴....~~

그렇다면 조건이 없을 때는 어떻게 할 것인가?

페이징처리 없이 가장 기본적인 방법을 고려해보자.

Chralie Parker의 음반 한장과 Pat Methney의 음반을 한장씩 한번 넣어보자.

```
{
    "musicianId" : 2,
    "albumTitle" : "Charlie Parker With Strings",
    "albumRelease" : "1950",
    "genre" : "Bop",
    "labelId" : 12
}

{
    "musicianId" : 3,
    "albumTitle" : "Offramp",
    "albumRelease" : "1982",
    "genre" : "Jazz",
    "labelId" : 1
}

```

완성된 코드는

```

    @Override
    public Flux<Musician> findMusicians() {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT musician.id AS 01_id, ");
        sb.append("       musician.name AS 02_name, ");
        sb.append("       musician.instrument AS 03_instrument, ");
        sb.append("       musician.birth AS 04_birth, ");
        sb.append("       musician.created_at AS 05_created, ");
        sb.append("       musician.updated_at AS 06_updated, ");
        sb.append("       album.id AS 07_albumId, ");
        sb.append("       album.title AS 08_title, ");
        sb.append("       album.release_year AS 09_releaseYear, ");
        sb.append("       album.genre AS 10_genre, ");
        sb.append("       label.id AS 11_labelId, ");
        sb.append("       label.name AS 12_labelName ");
        sb.append("     FROM musician");
        sb.append("     LEFT JOIN album ON musician.id = album.musician_id");
        sb.append("     JOIN label ON album.label_id = label.id");
        sb.append("     ORDER BY musician.id");

        return query.getDatabaseClient().sql(sb.toString())
                                        .fetch()
                                        .all()
                                        .bufferUntilChanged(result -> result.get("01_id"))
                                        .map(rows ->
                                                Musician.builder()
                                                        .id(parseLong(rows.get(0).get("01_id")))
                                                        .name(rows.get(0).get("02_name").toString())
                                                        .instrument(rows.get(0).get("03_instrument").toString())
                                                        .birth(rows.get(0).get("04_birth").toString())
                                                        .createdAt(toDateTime(rows.get(0).get("05_created")))
                                                        .updatedAt(toDateTime(rows.get(0).get("06_updated")))
                                                        .albums(rows.stream()
                                                                .map(row -> Album.builder()
                                                                        .id(row.get("07_albumId").toString())
                                                                        .title(row.get("08_title").toString())
                                                                        .releaseYear(row.get("09_releaseYear").toString())
                                                                        .genre(row.get("10_genre").toString())
                                                                        .label(Label.builder()
                                                                                .id(parseLong(row.get("11_labelId")))
                                                                                .name(row.get("12_labelName").toString())
                                                                                .build())
                                                                        .build())
                                                                .collect(toList()))
                                                        .build()
                                        );
    }

```
다만 주의할 점은 bufferUntilChanged id단위로 묶을 때 연속성을 가져야 한다. 예를 들면

```
musicianId|name         |instrument                                       |birth   |albumId                             |release_year|genre                     |labelId|labelName|
----------+-------------+-------------------------------------------------+--------+------------------------------------+------------+--------------------------+-------+---------+
1
1
1
2
2
2
1
1
3

```

이렇게 나왔다고 할때 위에 1을 묶음 3개의 row를 리스트로, 2를 묶음 3개 그리고 다시 1을 2개의 묶음으로 던지게 된다.

[bufferUntilChanged](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html)

해당 링크에서 bufferUntilChanged가 어떻게 동작하는지 확인해 보면 저 위에 말이 어떤 의미인지 알 수 있다.

따라서 소팅을 해줘야 한다.

속도 향상을 위해서 bufferUntilChanged를 사용하지만 저게 싫다면...

```
package io.basquiat.boards.music.repository.custom.impl;

import io.basquiat.boards.music.domain.entity.Album;
import io.basquiat.boards.music.domain.entity.Label;
import io.basquiat.boards.music.domain.entity.Musician;
import io.basquiat.boards.music.repository.custom.CustomMusicianRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.mapping.Column;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

import static io.basquiat.boards.common.utils.CommonUtils.toJson;
import static io.basquiat.boards.common.utils.DateUtils.toDateTime;
import static io.basquiat.boards.common.utils.NumberUtils.parseLong;
import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
public class CustomMusicianRepositoryImpl implements CustomMusicianRepository {

    private final R2dbcEntityTemplate query;

    @Override
    public Mono<Musician> findMusicianById(Long id) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT musician.id AS 01_id, ");
        sb.append("       musician.name AS 02_name, ");
        sb.append("       musician.instrument AS 03_instrument, ");
        sb.append("       musician.birth AS 04_birth, ");
        sb.append("       musician.created_at AS 05_created, ");
        sb.append("       musician.updated_at AS 06_updated, ");
        sb.append("       album.id AS 07_albumId, ");
        sb.append("       album.title AS 08_title, ");
        sb.append("       album.release_year AS 09_releaseYear, ");
        sb.append("       album.genre AS 10_genre, ");
        sb.append("       label.id AS 11_labelId, ");
        sb.append("       label.name AS 12_labelName ");
        sb.append("     FROM musician");
        sb.append("     LEFT JOIN album ON musician.id = album.musician_id");
        sb.append("     JOIN label ON album.label_id = label.id");
        sb.append("    WHERE musician.id = :id ");

        return query.getDatabaseClient().sql(sb.toString())
                                        .bind("id", id)
                                        .fetch()
                                        .all()
                                        .bufferUntilChanged(result -> result.get("01_id"))
                                        .map(rows ->
                                                Musician.builder()
                                                        .id(parseLong(rows.get(0).get("01_id")))
                                                        .name(rows.get(0).get("02_name").toString())
                                                        .instrument(rows.get(0).get("03_instrument").toString())
                                                        .birth(rows.get(0).get("04_birth").toString())
                                                        .createdAt(toDateTime(rows.get(0).get("05_created")))
                                                        .updatedAt(toDateTime(rows.get(0).get("06_updated")))
                                                        .albums(rows.stream()
                                                                    .map(row -> Album.builder()
                                                                            .id(row.get("07_albumId").toString())
                                                                            .title(row.get("08_title").toString())
                                                                            .releaseYear(row.get("09_releaseYear").toString())
                                                                            .genre(row.get("10_genre").toString())
                                                                            .label(Label.builder()
                                                                                        .id(parseLong(row.get("11_labelId")))
                                                                                        .name(row.get("12_labelName").toString())
                                                                                        .build())
                                                                            .build())
                                                                    .collect(toList()))
                                                        .build()
                                        )
                                        .take(1)
                                        .next();
    }

    @Override
    public Flux<Musician> findMusicians() {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT musician.id AS 01_id, ");
        sb.append("       musician.name AS 02_name, ");
        sb.append("       musician.instrument AS 03_instrument, ");
        sb.append("       musician.birth AS 04_birth, ");
        sb.append("       musician.created_at AS 05_created, ");
        sb.append("       musician.updated_at AS 06_updated, ");
        sb.append("       album.id AS 07_albumId, ");
        sb.append("       album.title AS 08_title, ");
        sb.append("       album.release_year AS 09_releaseYear, ");
        sb.append("       album.genre AS 10_genre, ");
        sb.append("       label.id AS 11_labelId, ");
        sb.append("       label.name AS 12_labelName ");
        sb.append("     FROM musician");
        sb.append("     LEFT JOIN album ON musician.id = album.musician_id");
        sb.append("     JOIN label ON album.label_id = label.id");

        return query.getDatabaseClient().sql(sb.toString())
                                        .fetch()
                                        .all()
                                        // 이 두 개는 주석을 풀어도 되고 주석처리를 해 된다.
                                        .bufferUntilChanged(result -> result.get("01_id"))
                                        .flatMapIterable(map -> map)
                                        //
                                        .groupBy(map -> map.get("01_id"), map -> map)
                                        .flatMap(flux -> flux.collectList()
                                                             .map(rows -> Musician.builder()
                                                                     .id(parseLong(rows.get(0).get("01_id")))
                                                                     .name(rows.get(0).get("02_name").toString())
                                                                     .instrument(rows.get(0).get("03_instrument").toString())
                                                                     .birth(rows.get(0).get("04_birth").toString())
                                                                     .createdAt(toDateTime(rows.get(0).get("05_created")))
                                                                     .updatedAt(toDateTime(rows.get(0).get("06_updated")))
                                                                     .albums(rows.stream()
                                                                             .map(row -> Album.builder()
                                                                                     .id(row.get("07_albumId").toString())
                                                                                     .title(row.get("08_title").toString())
                                                                                     .releaseYear(row.get("09_releaseYear").toString())
                                                                                     .genre(row.get("10_genre").toString())
                                                                                     .label(Label.builder()
                                                                                             .id(parseLong(row.get("11_labelId")))
                                                                                             .name(row.get("12_labelName").toString())
                                                                                             .build())
                                                                                     .build())
                                                                             .collect(toList()))
                                                                     .build())
                                        );
    }

}
```

groupBy를 이용하는 방법도 있다.

위에 코드에서 위 부분을 주석처리 했는데 그냥 저 위 부분을 유지하고 싶다면 저 주석을 풀어도 상관없다.

하지만 저 방식을 사용할 때 굳이 bufferUntilChanged를 사용할 이유가 없다.

```
// 이 두 개는 주석을 풀어도 되고 주석처리를 해 된다.
.bufferUntilChanged(result -> result.get("01_id"))
.flatMapIterable(map -> map)
//
```
어째든 이 코드의 경우에는 두개를 살려도 좋고 주석을 처리해 좋다.

리액티브 코드 스타일이 익숙하다면 저게 어떤 의미인지 잘 알듯.....

이쯤대면 이런 생각 들 것이다.

'우와 진짜... 주옥같은 Old School스타일이구나'

최종적으로 쿼리에 순번을 정해서 컬럼에 별칭을 주는 것은 미친 짓이다.

따라서 나는 fetch대신 map을 이용해서 row객체를 직접 다뤄 Map으로 바꾸고자 한다.

이게 dev.miku.r2dbc.mysql의 MySqlRowMetadata에서 벌어지는 이유인 듯 해서 패치 이전까지는 이 부분에 대한 mapper를 만들어서 사용한다.

컬럼이 몇개 안된다면야 컬럼명으로 소팅해서 사용하면 되고 조회할 컬럼이 많다면 한동안은 이것을 사용한다.

이것은 ColumnMapRowMapper 코드를 그대로 가져다 쓴다.

또한 groupBy을 사용해서 코드를 복잡하게 하는 것보다는 차리라 소팅을 해서 묶어 처리하는게 더 깔끔해 보이기 때문에

최종 코드는 다음과 같이 수정한다.

```
package io.basquiat.boards.common.mapper;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.Collection;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * RowToMapMapper
 *
 * MySqlRowMetadata에서 컬럼을 이름으로 소팅하는 부분이 있어서 그냥 날코딩으로 fetch대신 이것을 사용한다.
 *
 */
@Component
public class RowToMapMapper implements BiFunction<Row, RowMetadata, Map<String, Object>> {

    /**
     * 순수한 형태로 row to Map으로 변환한다.
     * @param row
     * @param rowMetadata
     * @return Map<String, Object>
     */
    @Override
    public Map<String, Object> apply(Row row, RowMetadata rowMetadata) {
        Collection<String> columns = rowMetadata.getColumnNames();
        int columnCount = columns.size();
        Map<String, Object> mapOfColValues = new LinkedCaseInsensitiveMap<>(columnCount);
        for (String column : columns) {
            String key = column;
            Object obj = row.get(key);
            mapOfColValues.put(key, obj);
        }
        return mapOfColValues;
    }

}


package io.basquiat.boards.music.repository.custom.impl;

import io.basquiat.boards.common.mapper.RowToMapMapper;
import io.basquiat.boards.music.domain.entity.Album;
import io.basquiat.boards.music.domain.entity.Label;
import io.basquiat.boards.music.domain.entity.Musician;
import io.basquiat.boards.music.repository.custom.CustomMusicianRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.util.LinkedCaseInsensitiveMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;

import static io.basquiat.boards.common.utils.DateUtils.toDateTime;
import static io.basquiat.boards.common.utils.NumberUtils.parseLong;
import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
public class CustomMusicianRepositoryImpl implements CustomMusicianRepository {

    private final R2dbcEntityTemplate query;

    private final RowToMapMapper rowToMapMapper;


    @Override
    public Mono<Musician> findMusicianById(Long id) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT musician.id AS id, ");
        sb.append("       musician.name AS name, ");
        sb.append("       musician.instrument AS instrument, ");
        sb.append("       musician.birth AS birth, ");
        sb.append("       musician.created_at AS created, ");
        sb.append("       musician.updated_at AS updated, ");
        sb.append("       album.id AS albumId, ");
        sb.append("       album.title AS title, ");
        sb.append("       album.release_year AS releaseYear, ");
        sb.append("       album.genre AS genre, ");
        sb.append("       label.id AS labelId, ");
        sb.append("       label.name AS labelName ");
        sb.append("     FROM musician");
        sb.append("     LEFT JOIN album ON musician.id = album.musician_id");
        sb.append("     JOIN label ON album.label_id = label.id");
        sb.append("    WHERE musician.id = :id ");

        return query.getDatabaseClient().sql(sb.toString())
                                        .bind("id", id)
                                        .map(rowToMapMapper::apply)
                                        .all()
                                        .bufferUntilChanged(result -> result.get("id"))
                                        .map(rows ->
                                                Musician.builder()
                                                        .id(parseLong(rows.get(0).get("id")))
                                                        .name(rows.get(0).get("name").toString())
                                                        .instrument(rows.get(0).get("instrument").toString())
                                                        .birth(rows.get(0).get("birth").toString())
                                                        .createdAt(toDateTime(rows.get(0).get("created")))
                                                        .updatedAt(toDateTime(rows.get(0).get("updated")))
                                                        .albums(rows.stream()
                                                                    .map(row -> Album.builder()
                                                                            .id(row.get("albumId").toString())
                                                                            .title(row.get("title").toString())
                                                                            .releaseYear(row.get("releaseYear").toString())
                                                                            .genre(row.get("genre").toString())
                                                                            .label(Label.builder()
                                                                                        .id(parseLong(row.get("labelId")))
                                                                                        .name(row.get("labelName").toString())
                                                                                        .build())
                                                                            .build())
                                                                    .collect(toList()))
                                                        .build()
                                        )
                                        .take(1)
                                        .next();
    }

    @Override
    public Flux<Musician> findMusicians() {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT musician.id, ");
        sb.append("       musician.name, ");
        sb.append("       musician.instrument, ");
        sb.append("       musician.birth, ");
        sb.append("       musician.created_at AS created, ");
        sb.append("       musician.updated_at AS updated, ");
        sb.append("       album.id AS albumId, ");
        sb.append("       album.title AS title, ");
        sb.append("       album.release_year AS releaseYear, ");
        sb.append("       album.genre AS genre, ");
        sb.append("       label.id AS labelId, ");
        sb.append("       label.name AS labelName ");
        sb.append("     FROM musician");
        sb.append("     LEFT JOIN album ON musician.id = album.musician_id");
        sb.append("     JOIN label ON album.label_id = label.id");
        sb.append("     ORDER BY musician.id");

        return query.getDatabaseClient().sql(sb.toString())
                                        .map(rowToMapMapper::apply)
                                        .all()
                                        .bufferUntilChanged(result -> result.get("id"))
                                        .map(rows ->
                                                Musician.builder()
                                                        .id(parseLong(rows.get(0).get("id")))
                                                        .name(rows.get(0).get("name").toString())
                                                        .instrument(rows.get(0).get("instrument").toString())
                                                        .birth(rows.get(0).get("birth").toString())
                                                        .createdAt(toDateTime(rows.get(0).get("created")))
                                                        .updatedAt(toDateTime(rows.get(0).get("updated")))
                                                        .albums(rows.stream()
                                                                .map(row -> Album.builder()
                                                                                 .id(row.get("albumId").toString())
                                                                                 .title(row.get("title").toString())
                                                                                 .releaseYear(row.get("releaseYear").toString())
                                                                                 .genre(row.get("genre").toString())
                                                                                 .label(Label.builder()
                                                                                             .id(parseLong(row.get("labelId")))
                                                                                             .name(row.get("labelName").toString())
                                                                                 .build())
                                                                        .build())
                                                                .collect(toList()))
                                                        .build()
                                        );
    }

}

```

OneToOne은 ManyToOne과 똑같은 방식으로 작업할 수 있다. 단지 1대1일라는 것 외에는 차이가 없다.

또한 위 OneToMany 관련 코드의 map에 저 괴랄하고 흉칙한 코드 부분은 매퍼를 사용해 깔끔하게 보일 수 있지만 그냥 놔둔다.

어짜피 반복되는 카피앤페이스트 작업의 연속일 뿐....

다음 브랜치에서는 R2DBC에서 제공하는 Criteria 형태의 코딩 스타일과 해도 그만 안해도 그만인 update/delete 관련 부분과 간단한 UI를 통해서 백오피스다운 앱을 하나 만들어 보고자 한다.

delete는 사실 삭제할 이유가 있을까?

그래도 일단 만들고 봐야....

## all()

all()의 전후 양상이 변하는 것을 볼 수 있는데 일종의 다음과 같은 코드를 생각하면 된다.

```

try{

    prepareStatement pstm = ....
    Result result = ....

} catch(Exception e) {
    ..
} finally {
    pstm.close()
    ..
}

```
즉 all 이전의 map에서는 인터페이스인 Row객체를 다루게 된다.

이것을 통해서 ResultSet의 정보를 가져오는 방식을 취하게 되는데 ManyToOne의 경우에는 직접적으로 엔티티 또는 DTO에 매핑이후

all()을 통해서 finally가 완성된다고 볼 수 있다.

그 이후 map에서는 Row객체를 사용할 수 없다. 만일

```
.map(row -> row)
//.fetch()
.all()
.map(row -> doSomething)
```
같은 코드를 사용하면 되지 않겠냐는 생각을 할 수 있는데 이렇게 되면 refCnt가 0이라는 메세지와 함께 에러가 발생된다는 것을 알 수 있다.

이유는 이미 닫힌 이후의 Row객체는 생명주기가 끝났기 때문이다.

OneToMany와 ManyToOne의 코드 스타일이 다른 것은 데이터를 어느 기준에 맞춰서 매핑을 하느냐에 따라 달라지는데

ManyToOne의 경우에는 직접적으로 Row객체를 생성해서 매핑하는 방식이 가능하다면 OneToMany는 그것이 안된다.

따라서 최종적으로 map()이나 FetchSpec<Map<String, Object>> 형태로 반환하는 fetch()이후 all()을 통해서 모든 정보를 수집하고 난 이후

그 데이터를 조작하는 방식으로 처리해야하는 것이다.

# At A Glance
코드가 참 괴랄하면서도 주옥같은 스타일이 많고 은근히 빡세다.

특히 reactive 코딩 스타일이 익숙하지 않거나 각 메소드의 기능과 역할을 제대로 이해하지 않으면 개발 자체가 퍼포먼스가 나올 수 있는 구조가 아니다.

그리고 몇몇 버그와 불편함이 있지만 좋아질것이라는 기대를 가지고 꾸준히 다뤄보는 수밖에....

다음 브랜치에서는 Criteria형식으로 작성하는 방법과 여유가 된다면 그럴싸한 UI를 이용해서 백오피스 메뉴처럼 진행할 예정이다.

길어지면 최종 UI 작업은 그 다음 브랜치로 넘기는 걸로.....
