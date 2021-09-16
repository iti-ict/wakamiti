package iti.kukumo.server.domain.model;

import java.time.*;

public class ExecutionCriteria {

    private int size = Integer.MAX_VALUE;
    private int page = 1;
    private LocalDate executionDate;
    private LocalDateTime executionIntervalFrom;
    private LocalDateTime executionIntervalTo;
    private boolean complete;
    private String owner;


    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public LocalDate getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(LocalDate executionDate) {
        this.executionDate = executionDate;
    }

    public LocalDateTime getExecutionIntervalFrom() {
        return executionIntervalFrom;
    }

    public void setExecutionIntervalFrom(LocalDateTime executionIntervalFrom) {
        this.executionIntervalFrom = executionIntervalFrom;
    }

    public LocalDateTime getExecutionIntervalTo() {
        return executionIntervalTo;
    }

    public void setExecutionIntervalTo(LocalDateTime executionIntervalTo) {
        this.executionIntervalTo = executionIntervalTo;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }
}