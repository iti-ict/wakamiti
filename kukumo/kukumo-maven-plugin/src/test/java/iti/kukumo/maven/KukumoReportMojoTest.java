package iti.kukumo.maven;

import imconfig.Configuration;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.core.Kukumo;
import iti.kukumo.maven.utils.KukumoAbstractMojoTest;
import iti.kukumo.maven.utils.ProjectStub;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class KukumoReportMojoTest extends KukumoAbstractMojoTest {

    private static final String GOAL = "report";
    private Kukumo kukumo;
    private Configuration currentConfiguration;

    protected void setUp() throws Exception {
        // required for mojo lookups to work
        super.setUp();

        kukumo = getKukumoMock();
        doAnswer((a) -> currentConfiguration = a.getArgument(0))
                .when(kukumo).generateReports(any());
    }

    //Test
    public void testWhenDefaultWithSuccess() throws Exception {
        // Given
        ProjectStub project = new ProjectStub(new File(getBasedir(), "src/test/resources/pom-default.xml"));
        MavenSession session = newMavenSession(project);

        // When
        KukumoReporterMojo mojo = (KukumoReporterMojo) executeMojo(session, GOAL);

        // Then
        assertThat(currentConfiguration).isEqualTo(KukumoConfiguration.DEFAULTS);

        assertThat(mojo.testFailureIgnore).isFalse();
        assertThat(mojo.configurationFiles).isEmpty();
        assertThat(mojo.properties).isEmpty();

        assertThat(session.getResult().getExceptions()).isEmpty();
        verify(kukumo, times(1)).generateReports(any());
    }

    //Test
    public void testWhenPropertiesWithSuccess() throws Exception {
        // Given
        ProjectStub project = new ProjectStub(new File(getBasedir(), "src/test/resources/pom-properties.xml"));
        Map<String, String> properties = getProjectProperties(project);
        MavenSession session = newMavenSession(project);

        // When
        KukumoReporterMojo mojo = (KukumoReporterMojo) executeMojo(session, GOAL);

        // Then
        assertThat(currentConfiguration.asMap()).containsAllEntriesOf(properties);

        assertThat(mojo.testFailureIgnore).isFalse();
        assertThat(mojo.configurationFiles).isEmpty();
        assertThat(mojo.properties).isEqualTo(properties);

        assertThat(session.getResult().getExceptions()).isEmpty();
        verify(kukumo, times(1)).generateReports(any());
    }

    //Test
    public void testWhenConfigWithSuccess() throws Exception {
        // Given
        ProjectStub project = new ProjectStub(new File(getBasedir(), "src/test/resources/pom-config.xml"));
        String configurationFiles = getProjectConfig(project, "configurationFiles");
        Map<String, String> properties = Configuration.factory().fromPath(Path.of(configurationFiles)).inner("kukumo").asMap();
        MavenSession session = newMavenSession(project);

        // When
        KukumoReporterMojo mojo = (KukumoReporterMojo) executeMojo(session, GOAL);

        // Then
        assertThat(currentConfiguration.asMap()).containsAllEntriesOf(properties);

        assertThat(mojo.testFailureIgnore).isFalse();
        assertThat(mojo.configurationFiles).isEqualTo(List.of(configurationFiles));
        assertThat(mojo.properties).isEmpty();

        assertThat(session.getResult().getExceptions()).isEmpty();
        verify(kukumo, times(1)).generateReports(any());
    }

    //Test
    public void testWhenIgnoreWithSuccess() throws Exception {
        // Given
        ProjectStub project = new ProjectStub(new File(getBasedir(), "src/test/resources/pom-ignore.xml"));
        MavenSession session = newMavenSession(project);

        // When
        KukumoReporterMojo mojo = (KukumoReporterMojo) executeMojo(session, GOAL);

        // Then
        assertThat(mojo.testFailureIgnore).isTrue();

        assertThat(session.getResult().getExceptions()).isEmpty();
        verify(kukumo, times(1)).generateReports(any());
    }

    //Test
    public void testWhenExceptionWithError() throws Exception {
        // Given
        doThrow(new RuntimeException("Error"))
                .when(kukumo).generateReports(any());

        ProjectStub project = new ProjectStub(new File(getBasedir(), "src/test/resources/pom-default.xml"));
        MavenSession session = newMavenSession(project);

        // When
        executeMojo(session, GOAL);

        // Then
        assertThat(session.getResult().getExceptions()).hasSize(1);
        assertThat(session.getResult().getExceptions().get(0)).isOfAnyClassIn(MojoExecutionException.class);
        assertThat(session.getResult().getExceptions().get(0)).hasCauseInstanceOf(RuntimeException.class);
        assertThat(session.getResult().getExceptions().get(0)).hasMessage("Kukumo configuration error: Error");
        verify(kukumo, times(1)).generateReports(any());
    }

    //Test
    public void testWhenExceptionAndIgnoreWithError() throws Exception {
        // Given
        doThrow(new RuntimeException("Error"))
                .when(kukumo).generateReports(any());

        ProjectStub project = new ProjectStub(new File(getBasedir(), "src/test/resources/pom-ignore.xml"));
        MavenSession session = newMavenSession(project);

        // When
        executeMojo(session, GOAL);

        // Then
        assertThat(session.getResult().getExceptions()).isEmpty();
        verify(kukumo, times(1)).generateReports(any());
    }
}
