package iti.kukumo.server.spi;

import java.time.Instant;
import java.util.*;

import iti.kukumo.server.domain.model.*;

public interface ExecutionRepository {

    Optional<KukumoExecution> getExecution(String owner, String executionID);

    boolean existsExecution(String owner, String executionID);

    List<KukumoExecution> getAllExecutions(String owner);

    List<String> getAllExecutionIDs(String owner);

    List<KukumoExecution> getExecutions(ExecutionCriteria criteria);

    List<String> getExecutionIDs(ExecutionCriteria criteria);

    void removeOldExecutions(int age);

	void saveExecution(KukumoExecution execution);

	Instant prepareExecution(String owner, String executionID);

}