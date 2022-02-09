# WebFlux로 백오피스 형식의 어플리케이션을 만들어 보자.

보통 백오피스를 굳이 웹플럭스를 활용해서 만들 필요가 있을까 생각이 든다.
하지만 아무래도 게시판 형식의 어플리케이션이 공부하기엔 최고라고 생각한다.
처음 IT에 발을 들였을때 선배들이 하나같이 하던 말이 있다.

"게시판 하나 끼깔나게 만들면 왠만한거 다 할 수 있어."

## Prerequisites

OS: macOS Monterey v12.2
JAVA: openjdk version "11.0.10" 2021-01-19
OS: macOS Catalina v10.15.5
IDE: IntelliJ IDEA 2020.1.2 (Community Edition)
Plugin: Lombok
Database: mySql 8.0.26
Spring Boot: 2.6.3
with Gradle

progress
Swagger : springfox-boot-starter 3.0.0

## 시작하기 전에
예전 심플하게 웹플럭스와 R2DBC를 활용한 토이 프로젝트를 공개한 적이 있었다.

당시에는 DataBaseClient를 이용한 방식을 활용했지만 이 프로젝트는 필요에 따라서 DataBaseClient의 Wrapper인 R2dbcEntityTemplate도 활용한다.

그리고 ReactiveTransactionManager을 별도로 설정하기 위해서 DatabaseConfiguration을 따로 작성한다.

첫 번째로 만들었던 녀석은 깊게 파지 않아서 좀 어설프고 쓸데없이 멋만 부린 코드들이 다수 존재해서 다 걷어낼 것이다.

그리고 무엇보다 변경된 것들이 좀 있어서 설정 부분의 변경점이 좀 있다. 

특히 r2dbc의 url 부분이 예전과는 좀 달라진듯 하다. 아니면 postGres에서 mySql로 바꿔서 그런건지 확인은 안해봤다. ~~일단 귀찮...~~

application.yml을 살펴보면 될 것 같다.

또한 R2DBC에서 제공하는 Repository는 Spring-Data에 기반하기 때문에 어느 면에서는 JPA와 비슷한 면이 존재하지만 결코 ORM이 아니다.

따라서 우리가 손쉬게 사용하는 익숙한 relation은 사용할 수 없다. 그래서 이것을 해결하기 위해 구현하는 방식이 몇가지 존재를 한다.

1. DataBaseClient를 이용해 직접적인 Join 쿼리를 작성하고 이것을 스트림을 통해 각각의 엔티티를 매핑한다.
   
2. Spring-Data의 API를 최대한 이용하고 Converter/Mapper을 활용해 이것을 처리한다.

둘다 짜증나긴 하지만 2번째 방법을 통해서 구현하는게 경험상 그나마 유지보수를 하는데 좀 수월하다는 개인적인 평가를 내렸기 때문에 2번째방법으로 구현한다.

~~첫 번째 방법이 맞는 분들도 있을테니 몇가지는 첫 번째 방법을 사용해서 해결해 본다.~~

다만 2번째 방법은 필요에 따라 그 만큼의 클래스를 생성해야 하는 수고로움이 존재하고 어플리케이션이 커지게 되면 관리 또한 복잡하게 된다.

그래서 상황에 따라서 선택지를 결정해야 한다.

## 최초 어플리케이션 설정 확인 테스

예전에 레디스 캐쉬 관련 작성했던 글을 그대로 사용해서 기본적인 crud중 create에 대한 부분만 테스트해 설정한 것들이 제대로 작동하는지 확인해 볼 생각이다.

```
CREATE TABLE `meta_data` (
  `id` int NOT NULL AUTO_INCREMENT,
  `meta_code` varchar(4) NOT NULL,
  `created_at` datetime NULL DEFAULT NULL,
  `updated_at` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
)
```
다음 테이블을 하나 생성하고 여기에 간략하게 api를 통해서 정보 하나를 생성하는 예제만 진행한다.

프로젝트의 meta 패키지를 확인해 보자.

MetaData.java
```
package io.basquiat.boards.meta.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Builder
@Data
@Table("meta_data")
@AllArgsConstructor
@RequiredArgsConstructor
public class MetaData {

    /** unique id */
    @Id
    @Column("id")
    private Long id;

    /** 메타 코드 */
    @Column("meta_code")
    private String metaCode;

    /** 등록일 */
    @Column("created_at")
    private LocalDateTime createdAt;

    /** 변경일 */
    @Column("updated_at")
    private LocalDateTime updatedAt;

}
```

MetaDataRepository.java
```
package io.basquiat.boards.meta.repository;

import io.basquiat.boards.meta.domain.MetaData;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface MetaDataRepository extends ReactiveCrudRepository<MetaData, Long> {
}
```

형식은 jpa와 상당히 유사한 부분이 있다. 

MetaDataService.java
```
package io.basquiat.boards.meta.service;

import io.basquiat.boards.meta.domain.MetaData;
import io.basquiat.boards.meta.repository.MetaDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import static java.time.LocalDateTime.now;

@Service
@RequiredArgsConstructor
public class MetaDataService {

    private final MetaDataRepository metaDataRepository;

    public Mono<MetaData> createMetaData(String metaCode) {
        MetaData metaData = MetaData.builder()
                                    .metaCode(metaCode)
                                    .createdAt(now())
                                    .build();
        return metaDataRepository.save(metaData);
    }

}

```

MetaDataController.java
```
package io.basquiat.boards.meta.web;

import io.basquiat.boards.meta.domain.MetaData;
import io.basquiat.boards.meta.service.MetaDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class MetaDataController {

    private final MetaDataService metaDataService;

    @PostMapping("/meta/{code}")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MetaData> createMetaData(@PathVariable("code") String code) {
        Assert.notNull(code, "must be code");
        return metaDataService.createMetaData(code);
    }

}

```

더 이상 자세한 섦명이 필요할까? 가장 흔한 스프링 어플리케이션과 크게 다르지 않다.

실제로 포스트맨을 통해서 'localhost:8080/v1/meta/TEST' 포스트 방법으로 날리게 되면

```
{
    "id": 1,
    "metaCode": "TEST",
    "createdAt": "2022-02-10T21:28:33.938236",
    "updatedAt": null
}
```
처럼 DB에 create한 이후 id를 반환해서 위와 같이 결과을 응답하면 설정은 끝이 났다.

시작이 반이다!

## 테스트 코드

여기서는 필요하다면 테스트 코드를 작성하겠지만 굳이 테스트를 위한 코드를 정성드려 작성할 생각이 없다.

## 컨셉

심플하게 좋아하는 뮤지션들의 음반 정보를 조회/수정/등록/삭제하는 일종의 뮤지션 정보를 관리하는 어드민이다.

# At A Glance
여기까지 왔다면 이제 컨셉에 맞춰서 어플리케이션을 만들면 된다.

다음 브랜치에서는 뮤지션 정보를 CRUD하는 것을 진행한다.
