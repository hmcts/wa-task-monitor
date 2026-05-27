package uk.gov.hmcts.reform.wataskmonitor.services.jobs.maintenance;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.regex.Pattern;
import javax.sql.DataSource;


@Service
@Slf4j
public class DatabaseMaintenanceExecutorService {
    private static final String SET_STATEMENT_TIMEOUT_SQL = "SELECT set_config('statement_timeout', ?, false)";

    private static final Pattern REINDEX_SQL =
        Pattern.compile("^REINDEX\\s+INDEX\\s+CONCURRENTLY\\s+CFT_TASK_DB\\.[A-Z_][A-Z0-9_]*\\s*;?$");
    private static final Pattern VACUUM_SQL =
        Pattern.compile("^VACUUM(\\s+ANALYZE)?\\s+CFT_TASK_DB\\.[A-Z_][A-Z0-9_]*\\s*;?$");

    private final DataSource dataSource;
    private final long maxTimeLimitSeconds;
    private final String sqlStatement;

    public DatabaseMaintenanceExecutorService(
        @Qualifier("databaseMaintenanceDataSource") DataSource dataSource,
        @Value("${job.database-maintenance.max-time-limit-seconds}") long maxTimeLimitSeconds,
        @Value("${job.database-maintenance.sql-statement}") String sqlStatement) {
        this.dataSource = dataSource;
        this.maxTimeLimitSeconds = maxTimeLimitSeconds;
        this.sqlStatement = sqlStatement;
    }

    public void executeConfiguredMaintenance() {
        execute(sqlStatement, maxTimeLimitSeconds);
    }

    public void execute(String sqlStatement, long timeoutSeconds) {
        validateSql(sqlStatement);

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            connection.setAutoCommit(true);
            applyStatementTimeout(connection, timeoutSeconds);
            statement.execute(sqlStatement);
            log.info("Database maintenance statement executed successfully: {}", sqlStatement);
        } catch (SQLException exception) {
            throw new IllegalStateException("Database maintenance execution failed", exception);
        }
    }

    void applyStatementTimeout(Connection connection, long timeoutSeconds) throws SQLException {
        try (PreparedStatement preparedStatement = connection.prepareStatement(SET_STATEMENT_TIMEOUT_SQL)) {
            preparedStatement.setString(1, timeoutSeconds + "s");
            preparedStatement.execute();
        }
    }

    void validateSql(String sqlStatement) {
        if (sqlStatement == null || sqlStatement.isBlank()) {
            throw new IllegalArgumentException("sql_statement is required for database maintenance");
        }

        String normalizedSql = sqlStatement.trim().toUpperCase(Locale.ENGLISH);
        if (!REINDEX_SQL.matcher(normalizedSql).matches() && !VACUUM_SQL.matcher(normalizedSql).matches()) {
            throw new IllegalArgumentException("Unsupported sql_statement for database maintenance");
        }
    }
}
