package iti.kukumo.server.spi;

import iti.kukumo.server.ExecutionCriteria;
import iti.kukumo.server.KukumoExecution;

import java.util.List;
import java.util.Optional;

public interface ExecutionRepository {

    Optional<KukumoExecution> getExecution(String executionID);

    boolean existsExecution(String executionID);

    List<KukumoExecution> getAllExecutions();

    List<String> getAllExecutionIDs();

    List<KukumoExecution> getExecutions(ExecutionCriteria criteria);

    List<String> getExecutionIDs(ExecutionCriteria criteria);

    void removeOldExecutions(int age);
}
