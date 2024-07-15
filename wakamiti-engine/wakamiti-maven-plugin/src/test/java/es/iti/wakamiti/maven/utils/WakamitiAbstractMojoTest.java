/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.maven.utils;


import es.iti.wakamiti.core.Wakamiti;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;


public abstract class WakamitiAbstractMojoTest extends AbstractMojoTestCase {

    private Wakamiti wakamiti;

    protected Wakamiti getWakamitiMock() {
        return wakamiti;
    }

    @Override
    protected void setUp() throws Exception {
        // required for mojo lookups to work
        super.setUp();

        wakamiti = mock(Wakamiti.class);

        Field instance = Wakamiti.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, wakamiti);
        Field instantiated = Wakamiti.class.getDeclaredField("instantiated");
        instantiated.setAccessible(true);
        ((AtomicBoolean) instantiated.get(null)).set(true);
    }

    protected Mojo executeMojo(MavenSession session, String goal) throws Exception {
        MojoExecution execution = newMojoExecution(goal);
        Plugin plugin = session.getCurrentProject().getPlugin(execution.getMojoDescriptor()
                .getPluginDescriptor().getPluginLookupKey());
        execution.getMojoDescriptor().getPluginDescriptor().setPlugin(plugin);
        Mojo mojo = lookupConfiguredMojo(session, execution);
        mojo.setLog(new TestLogger());
        assertThat(mojo).isNotNull();
        try {
            mojo.execute();
        } catch (Throwable e) {
            session.getResult().addException(e);
        }
        return mojo;
    }

    protected Map<String, String> getProjectProperties(MavenProject project) {
        Plugin plugin = project.getBuildPlugins().get(0);
        return Arrays.stream(((Xpp3Dom) plugin.getConfiguration()).getChild("properties").getChildren())
                .collect(Collectors.toMap(Xpp3Dom::getName, Xpp3Dom::getValue));
    }

    protected String getProjectConfig(MavenProject project, String property) {
        Plugin plugin = project.getBuildPlugins().get(0);
        return ((Xpp3Dom) plugin.getConfiguration()).getChild(property).getValue();
    }
}
