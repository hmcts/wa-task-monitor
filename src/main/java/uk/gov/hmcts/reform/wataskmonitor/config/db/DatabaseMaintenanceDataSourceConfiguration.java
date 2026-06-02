package uk.gov.hmcts.reform.wataskmonitor.config.db;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class DatabaseMaintenanceDataSourceConfiguration {

    @Bean
    @Qualifier("databaseMaintenanceDataSource")
    public DataSource databaseMaintenanceDataSource(
        @Value("${job.database-maintenance.jdbc-url}") String jdbcUrl,
        @Value("${job.database-maintenance.username}") String username,
        @Value("${job.database-maintenance.password}") String password) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(jdbcUrl);
        dataSource.setUsername(username);
        dataSource.setPassword(password);
        return dataSource;
    }
}
