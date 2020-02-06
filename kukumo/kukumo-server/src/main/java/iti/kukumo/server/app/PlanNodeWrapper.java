package iti.kukumo.server.app;

import iti.kukumo.api.Kukumo;
import iti.kukumo.api.plan.PlanNodeSnapshot;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PlanNodeWrapper {

    private Map<String,String> metadata;
    private PlanNodeSnapshot data;

    public PlanNodeWrapper(Map<String, String> metadata, PlanNodeSnapshot data) {
        this.metadata = Map.copyOf(metadata);
        this.data = data;
    }

    public String serialize() throws IOException {
        StringBuilder string = new StringBuilder("{");
        if (!metadata.isEmpty()) {
            string.append(
            metadata.entrySet().stream()
                .map(e->String.format("\"%s\":\"%s\"",e.getKey(),e.getValue()))
                .collect(Collectors.joining(",","\"metadata\": {","},"))
            );
        }
        string.append("\"data\":").append(Kukumo.planSerializer().serialize(data)).append("}");
        return string.toString();
    }



}
