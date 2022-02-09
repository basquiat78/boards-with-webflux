package io.basquiat.boards.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * my sql info
 * created by basquiat
 */
@Data
@Component
@ConfigurationProperties(prefix = "mysql")
public class MySqlProperty {

    private String host;
    private int port;
    private String username;
    private String password;
    private String database;

}
