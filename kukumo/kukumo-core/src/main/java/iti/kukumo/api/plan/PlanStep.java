package iti.kukumo.api.plan;

import java.time.Instant;
import java.util.Optional;

public interface PlanStep extends PlanNode {

    Optional<Document> getDocument();
    Optional<DataTable> getDataTable();
    Optional<Throwable> getError();
    Instant getStartInstant();
    Instant getFinishInstant();
    Result getResult();
    boolean isBackgroundStep();
    boolean isVoid();

    void markStarted(Instant instant);
    void markPassed(Instant instant);
    void markFailure(Instant instant, Result result, Throwable error);

}
