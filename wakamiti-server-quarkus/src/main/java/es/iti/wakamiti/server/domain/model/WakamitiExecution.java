/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.server.domain.model;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import es.iti.wakamiti.api.WakamitiAPI;
import es.iti.wakamiti.api.plan.*;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;

@JsonSerialize()
public class WakamitiExecution {



    public static class SnapshotSerializer extends StdSerializer<PlanNodeSnapshot> {

        private static final long serialVersionUID = 1858936262551275169L;

        public SnapshotSerializer() {
            super(PlanNodeSnapshot.class);
        }

        @Override
        public void serialize(PlanNodeSnapshot planNodeSnapshot, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeRaw(": ");
            jsonGenerator.writeRaw(WakamitiAPI.instance().planSerializer().serialize(planNodeSnapshot));
        }
    }




    public static WakamitiExecution fromSnapshot(PlanNodeSnapshot snapshot, String owner) {
        return new WakamitiExecution(
            snapshot,
            snapshot.getExecutionID(),
            snapshot.getSnapshotInstant(),
            owner
        );
    }

    public static WakamitiExecution fromResult(PlanNode result, String owner) {
        return fromSnapshot(new PlanNodeSnapshot(result), owner);
    }

    public static WakamitiExecution fromPlan(
        PlanNode plan,
        String executionID,
        String instant,
        String owner
    ) {
        var snapshot = new PlanNodeSnapshot(plan);;
        return new WakamitiExecution(snapshot, executionID, instant, owner);
    }


    public static WakamitiExecution fromPlan(PlanNode plan, String owner) {
        return fromSnapshot(new PlanNodeSnapshot(plan), owner);
    }



    private String executionID;
    private String executionInstant;
    private String owner;
    @JsonSerialize(using = SnapshotSerializer.class)
    private PlanNodeSnapshot data;




    private WakamitiExecution(PlanNodeSnapshot data, String executionID, String instant, String owner) {
        this.data = data;
        this.executionID = executionID;
        this.executionInstant = instant;
        this.owner = owner;
    }


    public PlanNodeSnapshot getData() {
        return data;
    }


    public String getExecutionID() {
        return executionID;
    }

    public String getExecutionInstant() {
        return executionInstant;
    }

    public String getOwner() {
        return owner;
    }
}