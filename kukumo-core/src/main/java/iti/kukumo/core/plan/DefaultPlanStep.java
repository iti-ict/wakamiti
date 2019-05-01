package iti.kukumo.core.plan;

import java.time.Instant;
import java.util.Optional;

import iti.kukumo.api.plan.DataTable;
import iti.kukumo.api.plan.Document;
import iti.kukumo.api.plan.PlanNodeTypes;
import iti.kukumo.api.plan.PlanStep;
import iti.kukumo.api.plan.Result;

public class DefaultPlanStep extends DefaultPlanNode<DefaultPlanStep> implements PlanStep {

    private Document document;
    private DataTable dataTable;
    private Throwable error;
    private Result result;
    private Instant startInstant;
    private Instant finishInstant;
    private boolean isBackgroundStep;


    public DefaultPlanStep() {
        super(PlanNodeTypes.STEP);
    }


    @Override
    public Optional<Document> getDocument() {
        return Optional.ofNullable(document);
    }


    @Override
    public Optional<DataTable> getDataTable() {
        return Optional.ofNullable(dataTable);
    }


    @Override
    public Optional<Throwable> getError() {
        return Optional.ofNullable(error);
    }


    @Override
    public Instant getStartInstant() {
        return startInstant;
    }

    @Override
    public Instant getFinishInstant() {
        return finishInstant;
    }

    @Override
    public Result getResult() {
        return result;
    }

    @Override
    public boolean isBackgroundStep() {
        return isBackgroundStep;
    }


    @Override
    public Optional<Instant> computeStartInstant() {
        return hasChildren() ? super.computeStartInstant() : Optional.ofNullable(startInstant);
    }


    @Override
    public Optional<Instant> computeFinishInstant() {
        return hasChildren() ? super.computeFinishInstant() : Optional.ofNullable(finishInstant);
    }

    @Override
    public Optional<Result> computeResult() {
        return hasChildren() ? super.computeResult() : Optional.ofNullable(result);
    }


    @Override
    public void markStarted(Instant instant) {
        assertNotStarted();
        this.startInstant = instant;
    }


    @Override
    public void markPassed(Instant instant) {
        assertNotFinished();
        this.finishInstant = instant;
        this.result = Result.PASSED;
    }


    @Override
    public void markFailure(Instant instant, Result result, Throwable error) {
        assertNotFinished();
        this.finishInstant = instant;
        this.result = result;
        this.error = error;
    }

    
    private void assertNotStarted() throws IllegalStateException {
        if (this.startInstant != null) {
            throw new IllegalStateException("Step already started");
        }
    }
    
    private void assertNotFinished() throws IllegalStateException {
        if (this.startInstant == null) {
            throw new IllegalStateException("Step not started");
        }
        if (this.result != null) {
            throw new IllegalStateException("Step already finsihed");
        }
    }
    
    
    
    public DefaultPlanStep setBackgroundStep(boolean isBackgroundStep) {
        this.isBackgroundStep = isBackgroundStep;
        return this;
    }


    public DefaultPlanStep setDocument(Document document) {
        this.document = document;
        return this;
    }

    public DefaultPlanStep setDataTable(DataTable dataTable) {
        this.dataTable = dataTable;
        return this;
    }

    



    @Override
    public DefaultPlanStep copy() {
        return this.copy(new DefaultPlanStep());
    }

    protected DefaultPlanStep copy(DefaultPlanStep copy) {
        copy.document = (this.document == null ? null : this.document.copy());
        copy.dataTable = (this.dataTable == null ? null : this.dataTable.copy());
        copy.error = this.error;
        copy.isBackgroundStep = this.isBackgroundStep;
        super.copy(copy);
        return copy;
    }


    public DefaultPlanNode<?> copyAsNode() {
        return super.copy();
    }





    @Override
    public boolean isVoid() {
        return false;
    }

}
