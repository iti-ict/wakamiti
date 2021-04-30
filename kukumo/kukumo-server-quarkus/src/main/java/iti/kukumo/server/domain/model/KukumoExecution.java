package iti.kukumo.server.domain.model;

import java.io.IOException;
import java.time.LocalDateTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import iti.kukumo.api.Kukumo;
import iti.kukumo.api.plan.PlanNodeSnapshot;

@JsonSerialize()
public class KukumoExecution {

    public static class SnapshotSerializer extends StdSerializer<PlanNodeSnapshot> {

    	private static final long serialVersionUID = 1858936262551275169L;

		public SnapshotSerializer() {
            super(PlanNodeSnapshot.class);
        }

        @Override
        public void serialize(PlanNodeSnapshot planNodeSnapshot, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeRaw(": ");
            jsonGenerator.writeRaw(Kukumo.planSerializer().serialize(planNodeSnapshot));
        }
    }


    private String executionID;
    private String executionInstant;
    @JsonSerialize(using = SnapshotSerializer.class)
    private PlanNodeSnapshot data;




    public KukumoExecution(PlanNodeSnapshot data) {
        this(data, data.getExecutionID(), data.getStartInstant());
    }


    public KukumoExecution(PlanNodeSnapshot data, String executionID, String instant) {
        this.data = data;
        this.executionID = executionID;
        this.executionInstant = instant;
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

}
