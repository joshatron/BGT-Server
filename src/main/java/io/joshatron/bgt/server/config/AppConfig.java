package io.joshatron.bgt.server.config;

import io.joshatron.bgt.server.database.SqliteManager;
import io.joshatron.bgt.server.exceptions.GameServerException;
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
