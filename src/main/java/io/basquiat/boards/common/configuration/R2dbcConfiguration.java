package io.basquiat.boards.common.configuration;


import io.basquiat.boards.music.converter.AlbumConverter;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

import java.util.ArrayList;
import java.util.List;

/**
 * r2dbc configuration
 * created by basquiat
 */
@Configuration
@EnableR2dbcRepositories
public class R2dbcConfiguration extends AbstractR2dbcConfiguration {

    @Override
    public ConnectionFactory connectionFactory() {
        return null;
    }

    /**
     * converter를 등록한다.
     * @return List<Object>
     */
    @Override
    protected List<Object> getCustomConverters() {
        List<Object> converterList = new ArrayList<>();
        converterList.add(new AlbumConverter());
        return converterList;
    }

}
