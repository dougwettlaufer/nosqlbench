package io.nosqlbench.activitytype.jdbc.impl;

import com.codahale.metrics.Timer;
import io.nosqlbench.activitytype.jdbc.api.JDBCActivity;
import io.nosqlbench.engine.api.activityapi.core.SyncAction;
import io.nosqlbench.engine.api.activityapi.planning.OpSequence;
import io.nosqlbench.engine.api.activityimpl.OpDispenser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;
import java.util.function.LongFunction;

public class JDBCAction implements SyncAction {
    private static final Logger LOGGER = LogManager.getLogger(JDBCAction.class);

    private final JDBCActivity activity;
    private OpSequence<OpDispenser<String>> sequencer;

    public JDBCAction(JDBCActivity a, int slot) {
        activity = a;
    }

    @Override
    public void init() {
        sequencer = activity.getOpSequence();
    }

    @Override
    public int runCycle(long cycle) {
        String boundStmt;

        LongFunction<String> unboundStmt = sequencer.apply(cycle);

        try (Timer.Context bindTime = activity.getBindTimer().time()) {
            boundStmt = unboundStmt.apply(cycle);
        }

        int maxTries = activity.getMaxTries();
        int errorCode = 0;

        for (int tries = 1; tries <= maxTries; tries++) {
            errorCode = execute(boundStmt, tries);
            if (errorCode == 0) return 0;
        }

        LOGGER.debug("Max tries " + maxTries + " exceeded for executing statement " + boundStmt);
        return errorCode;
    }

    private int execute(String sql, int tries) {
        long startTimeNano = System.nanoTime();
        Long resultTime = null;

        try (Connection conn = activity.getDataSource().getConnection()) {
            Statement jdbcStmt = conn.createStatement();
            jdbcStmt.execute(sql);

            resultTime = System.nanoTime() - startTimeNano;
            activity.getResultSuccessTimer().update(resultTime, TimeUnit.NANOSECONDS);

        } catch (Exception e) {
            LOGGER.debug("Try " + tries + ": failed to execute statement: " + sql, e);

            activity.getExceptionCount().count(e);

            if (e instanceof SQLException) {
                SQLException sqle = (SQLException) e;

                activity.getSQLExceptionCount().inc(sqle);

                // TODO non-retryable exception should return its non-zero error code to runCycle() caller
                if (!activity.isRetryable(sqle)) {
                    return 0;
                }

                return sqle.getErrorCode();
            }

            return 1;

        } finally {
            if (resultTime == null) {
                resultTime = System.nanoTime() - startTimeNano;
            }

            activity.getResultTimer().update(resultTime, TimeUnit.NANOSECONDS);
            activity.getTriesHisto().update(tries);
        }

        LOGGER.trace("Try " + tries + ": successfully executed statement: " + sql);
        return 0;
    }

}
