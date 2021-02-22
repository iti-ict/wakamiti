package iti.kukumo.server.infra.app;

import iti.kukumo.api.Kukumo;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;
import java.util.TreeMap;

@Path("configuration")
public class ConfigurationResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String,String> getConfiguration() {
        return new TreeMap<>(Kukumo.contributors().globalDefaultConfiguration().asMap());
    }

}
