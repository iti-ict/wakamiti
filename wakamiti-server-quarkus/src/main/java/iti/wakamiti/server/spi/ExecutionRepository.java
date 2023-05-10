/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.wakamiti.server.spi;

import java.time.Instant;
import java.util.*;

import iti.wakamiti.server.domain.model.*;

public interface ExecutionRepository {

    Optional<WakamitiExecution> getExecution(String owner, String executionID);

    boolean existsExecution(String owner, String executionID);

    List<WakamitiExecution> getAllExecutions(String owner);

    List<String> getAllExecutionIDs(String owner);

    List<WakamitiExecution> getExecutions(ExecutionCriteria criteria);

    List<String> getExecutionIDs(ExecutionCriteria criteria);

    void removeOldExecutions(int age);

	void saveExecution(WakamitiExecution execution);

	Instant prepareExecution(String owner, String executionID);

}