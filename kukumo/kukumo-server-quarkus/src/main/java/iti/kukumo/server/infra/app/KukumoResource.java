package iti.kukumo.server.infra.app;

import iti.commons.jext.Extension;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.extensions.Contributor;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Path("kukumo")
public class KukumoResource {

    @GET
    @Path("configuration")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String,String> getConfiguration() {
        return new TreeMap<>(Kukumo.contributors().globalDefaultConfiguration().asMap());
    }

    @GET
    @Path("contributors")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<Object, Object> getContributors() {
        return Kukumo.contributors()
		.allContributors().entrySet()
		.stream().collect(Collectors.toMap(
			e->e.getKey().getCanonicalName(),
			e->e.getValue().stream().map(Contributor::info).collect(Collectors.toList())
		));
    }

}
