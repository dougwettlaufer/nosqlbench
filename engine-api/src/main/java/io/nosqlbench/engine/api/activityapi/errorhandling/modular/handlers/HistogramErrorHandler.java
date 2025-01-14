package io.nosqlbench.engine.api.activityapi.errorhandling.modular.handlers;

import io.nosqlbench.engine.api.activityapi.errorhandling.ErrorMetrics;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorDetail;
import io.nosqlbench.engine.api.activityapi.errorhandling.modular.ErrorHandler;
import io.nosqlbench.engine.api.metrics.ExceptionHistoMetrics;
import io.nosqlbench.nb.annotations.Service;

import java.util.function.Supplier;

/**
 * The histogram error handler will create, if needed, a histogram metrics
 * object for the named {@code "errorhistos.simplename" + e.getClass().getSimpleName()},
 * and then add the current session time to it.
 */
@Service(value = ErrorHandler.class, selector = "histogram")
public class HistogramErrorHandler implements ErrorHandler, ErrorMetrics.Aware {
    private ExceptionHistoMetrics exceptionHistoMetrics;

    @Override
    public ErrorDetail handleError(Throwable t, long cycle, long durationInNanos, ErrorDetail detail) {
        exceptionHistoMetrics.update(t, durationInNanos);
        return detail;
    }

    @Override
    public void setErrorMetricsSupplier(Supplier<ErrorMetrics> supplier) {
        this.exceptionHistoMetrics = supplier.get().getExceptionHistoMetrics();
    }
}
