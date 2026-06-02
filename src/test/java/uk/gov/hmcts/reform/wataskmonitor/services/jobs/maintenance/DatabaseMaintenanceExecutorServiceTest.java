package uk.gov.hmcts.reform.wataskmonitor.services.jobs.maintenance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DatabaseMaintenanceExecutorServiceTest {

    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private Statement statement;
    @Mock
    private PreparedStatement preparedStatement;

    private DatabaseMaintenanceExecutorService executorService;

    @BeforeEach
    void setUp() {
        executorService = new DatabaseMaintenanceExecutorService(
            dataSource,
            300L,
            "VACUUM ANALYZE cft_task_db.tasks"
        );
    }

    @Test
    void should_execute_maintenance_statement_on_dedicated_connection() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(connection.prepareStatement("SELECT set_config('statement_timeout', ?, false)"))
            .thenReturn(preparedStatement);

        executorService.executeConfiguredMaintenance();

        verify(connection).setAutoCommit(true);
        verify(preparedStatement).setString(1, "300s");
        verify(preparedStatement).execute();
        verify(statement).execute("VACUUM ANALYZE cft_task_db.tasks");
    }

    @Test
    void should_reject_unsupported_sql_statement() {
        assertThatThrownBy(() -> executorService.execute("DROP TABLE cft_task_db.tasks", 300L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unsupported sql_statement for database maintenance");
    }

    @Test
    void should_fail_when_sql_statement_missing() {
        assertThatThrownBy(() -> executorService.execute("   ", 300L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("sql_statement is required for database maintenance");
    }
}
