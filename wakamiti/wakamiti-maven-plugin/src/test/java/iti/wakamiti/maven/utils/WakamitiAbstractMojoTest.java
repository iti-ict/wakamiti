package iti.wakamiti.maven.utils;

import iti.wakamiti.core.Wakamiti;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.Mojo;
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
        Mojo mojo = lookupConfiguredMojo(session, newMojoExecution(goal));
        assertThat(mojo).isNotNull();
        mojo.execute();
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
