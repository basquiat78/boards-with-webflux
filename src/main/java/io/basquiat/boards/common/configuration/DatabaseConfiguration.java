package io.basquiat.boards.common.configuration;

import dev.miku.r2dbc.mysql.MySqlConnectionConfiguration;
import dev.miku.r2dbc.mysql.MySqlConnectionFactory;
import io.basquiat.boards.common.properties.MySqlProperty;
import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;

/**
 * databaseConfig
 * created by basquiat
 */
@Configuration
@EnableR2dbcRepositories
@RequiredArgsConstructor
public class DatabaseConfiguration extends AbstractR2dbcConfiguration {

    private final MySqlProperty mySql;

    @Override
    public ConnectionFactory connectionFactory() {
        return MySqlConnectionFactory.from(MySqlConnectionConfiguration.builder()
                                                                       .host(mySql.getHost())
                                                                       .port(mySql.getPort())
                                                                       .username(mySql.getUsername())
                                                                       .password(mySql.getPassword())
                                                                       .database(mySql.getDatabase())
                                                                       .build());
    }

    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

}
