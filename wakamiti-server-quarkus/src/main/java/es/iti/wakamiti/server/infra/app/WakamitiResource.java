/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.server.infra.app;

import java.util.*;
import java.util.stream.Collectors;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.api.extensions.Contributor;

@Path("wakamiti")
public class WakamitiResource {

    @GET
    @Path("configuration")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String,String> getConfiguration() {
        return new TreeMap<>(Wakamiti.contributors().globalDefaultConfiguration().asMap());
    }

    @GET
    @Path("contributors")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<Object, Object> getContributors() {
        return Wakamiti.contributors()
		.allContributors().entrySet()
		.stream().collect(Collectors.toMap(
			e->e.getKey().getCanonicalName(),
			e->e.getValue().stream().map(Contributor::info).collect(Collectors.toList())
		));
    }

}