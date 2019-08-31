package io.joshatron.tak.server.config;

import io.joshatron.tak.server.database.*;
import io.joshatron.tak.server.exceptions.GameServerException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.sql.Connection;

@Configuration
@EnableAsync
public class AppConfig {

    @Bean
    public Connection connection() throws GameServerException {
        return SqliteManager.getConnection();
    }
}
