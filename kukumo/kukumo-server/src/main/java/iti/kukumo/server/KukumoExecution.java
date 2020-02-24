package iti.kukumo.server;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.plan.PlanNodeSnapshot;

import java.io.IOException;
import java.util.Map;

@JsonSerialize()
public class KukumoExecution {

    public static class SnapshotSerializer extends StdSerializer<PlanNodeSnapshot> {

        public SnapshotSerializer() {
            super(PlanNodeSnapshot.class);
        }

        @Override
        public void serialize(PlanNodeSnapshot planNodeSnapshot, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeRaw(": ");
            jsonGenerator.writeRaw(Kukumo.planSerializer().serialize(planNodeSnapshot));
        }
    }


    private Map<String,String> metadata;

    @JsonSerialize(using = SnapshotSerializer.class)
    private PlanNodeSnapshot data;


    public KukumoExecution(Map<String, String> metadata, PlanNodeSnapshot data) {
        this.metadata = Map.copyOf(metadata);
        this.data = data;
    }


    public KukumoExecution(PlanNodeSnapshot data) {
        this.metadata = Map.of();
        this.data = data;
    }


}
