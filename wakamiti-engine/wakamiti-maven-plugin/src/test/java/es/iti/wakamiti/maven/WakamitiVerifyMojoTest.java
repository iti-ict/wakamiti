package es.iti.wakamiti.maven;

import es.iti.wakamiti.core.Wakamiti;
import es.iti.wakamiti.maven.utils.ProjectStub;
import es.iti.wakamiti.maven.utils.WakamitiAbstractMojoTest;
import imconfig.Configuration;
import es.iti.wakamiti.api.WakamitiConfiguration;
import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.plan.PlanNode;
import es.iti.wakamiti.api.plan.Result;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class WakamitiVerifyMojoTest extends WakamitiAbstractMojoTest {

    private static final String GOAL = "verify";

    private PlanNode plan;
    private Configuration currentConfiguration;

    protected void setUp() throws Exception {
        // required for mojo lookups to work
        super.setUp();

        plan = mock(PlanNode.class);
        Wakamiti wakamiti = getWakamitiMock();

        when(wakamiti.createPlanFromConfiguration(any(Configuration.class)))
                .then((a) -> {
                    currentConfiguration = a.getArgument(0);
                    return plan;
                });
        when(wakamiti.executePlan(any(), any())).thenReturn(plan);
    }


    //Test
    public void testWhenDefaultWithSuccess() throws Exception {
        // Given
        when(plan.result()).thenReturn(Optional.of(Result.PASSED));
        when(plan.hasChildren()).thenReturn(true);

        ProjectStub project = new ProjectStub(new File(getBasedir(), "src/test/resources/pom-default.xml"));
        MavenSession session = newMavenSession(project);

        // When
        WakamitiVerifyMojo mojo = (WakamitiVerifyMojo) executeMojo(session, GOAL);

        // Then
        assertThat(currentConfiguration).isEqualTo(WakamitiConfiguration.DEFAULTS);

        assertThat(mojo.testFailureIgnore).isFalse();
        assertThat(mojo.configurationFiles).isEmpty();
        assertThat(mojo.logLevel).isEqualTo("info");
        assertThat(mojo.skipTests).isFalse();
        assertThat(mojo.properties).isEmpty();

        assertThat(session.getResult().getExceptions()).isEmpty();
        verify(plan, times(1)).result();
    }

    //Test
    public void testWhenPropertiesWithSuccess() throws Exception {
        // Given
        when(plan.result()).thenReturn(Optional.of(Result.PASSED));
        when(plan.hasChildren()).thenReturn(true);

        ProjectStub project = new ProjectStub(new File(getBasedir(), "src/test/resources/pom-properties.xml"));
        Map<String, String> properties = getProjectProperties(project);
        MavenSession session = newMavenSession(project);

        // When
        WakamitiVerifyMojo mojo = (WakamitiVerifyMojo) executeMojo(session, GOAL);

        // Then
        assertThat(currentConfiguration.asMap()).containsAllEntriesOf(properties);

        assertThat(mojo.testFailureIgnore).isFalse();
        assertThat(mojo.configurationFiles).isEmpty();
        assertThat(mojo.logLevel).isEqualTo("info");
        assertThat(mojo.skipTests).isFalse();
        assertThat(mojo.properties).isEqualTo(properties);

        assertThat(session.getResult().getExceptions()).isEmpty();
        verify(plan, times(1)).result();
    }

    //Test
    public void testWhenConfigWithSuccess() throws Exception {
        // Given
        when(plan.result()).thenReturn(Optional.of(Result.PASSED));
        when(plan.hasChildren()).thenReturn(true);

        ProjectStub project = new ProjectStub(new File(getBasedir(), "src/test/resources/pom-config.xml"));
        String configurationFiles = getProjectConfig(project, "configurationFiles");
        Map<String, String> properties = Configuration.factory().fromPath(Path.of(configurationFiles)).inner("wakamiti").asMap();
        MavenSession session = newMavenSession(project);

        // When
        WakamitiVerifyMojo mojo = (WakamitiVerifyMojo) executeMojo(session, GOAL);

        // Then
        assertThat(currentConfiguration.asMap()).containsAllEntriesOf(properties);

        assertThat(mojo.testFailureIgnore).isFalse();
        assertThat(mojo.configurationFiles).isEqualTo(List.of(configurationFiles));
        assertThat(mojo.logLevel).isEqualTo("info");
        assertThat(mojo.skipTests).isFalse();
        assertThat(mojo.properties).isEmpty();

        assertThat(session.getResult().getExceptions()).isEmpty();
        verify(plan, times(1)).result();
    }

    //Test
    public void testWhenSkipTestWithSuccess() throws Exception {
        // Given
        ProjectStub project = new ProjectStub(new File(getBasedir(), "src/test/resources/pom-skip.xml"));
        MavenSession session = newMavenSession(project);

        // When
        WakamitiVerifyMojo mojo = (WakamitiVerifyMojo) executeMojo(session, GOAL);

        // Then
        assertThat(mojo.skipTests).isTrue();

        assertThat(session.getResult().getExceptions()).isEmpty();
        verify(plan, times(0)).hasChildren();
        verify(plan, times(0)).result();
    }

    //Test
    public void testWhenLogLevelWithSuccess() throws Exception {
        // Given
        when(plan.result()).thenReturn(Optional.of(Result.PASSED));
        when(plan.hasChildren()).thenReturn(true);

        ProjectStub project = new ProjectStub(new File(getBasedir(), "src/test/resources/pom-log.xml"));
        MavenSession session = newMavenSession(project);

        // When
        WakamitiVerifyMojo mojo = (WakamitiVerifyMojo) executeMojo(session, GOAL);

        // Then
        assertThat(mojo.logLevel).isEqualTo("debug");

        assertThat(session.getResult().getExceptions()).isEmpty();
        verify(plan, times(1)).result();
    }

    //Test
    public void testWhenIgnoreWithSuccess() throws Exception {
        // Given
        when(plan.result()).thenReturn(Optional.of(Result.PASSED));
        when(plan.hasChildren()).thenReturn(true);

        ProjectStub project = new ProjectStub(new File(getBasedir(), "src/test/resources/pom-ignore.xml"));
        MavenSession session = newMavenSession(project);

        // When
        WakamitiVerifyMojo mojo = (WakamitiVerifyMojo) executeMojo(session, GOAL);

        // Then
        assertThat(mojo.testFailureIgnore).isTrue();

        assertThat(session.getResult().getExceptions()).isEmpty();
        verify(plan, times(1)).result();
    }

    //Test
    public void testWhenNotPassedWithError() throws Exception {
        // Given
        when(plan.result()).thenReturn(Optional.of(Result.FAILED));
        when(plan.hasChildren()).thenReturn(true);

        ProjectStub project = new ProjectStub(new File(getBasedir(), "src/test/resources/pom-default.xml"));
        MavenSession session = newMavenSession(project);

        // When
        executeMojo(session, GOAL);

        // Then
        assertThat(session.getResult().getExceptions()).hasSize(1);
        assertThat(session.getResult().getExceptions().get(0)).isOfAnyClassIn(MojoFailureException.class);
        assertThat(session.getResult().getExceptions().get(0)).hasMessage("Wakamiti Test Plan not passed");
        verify(plan, times(1)).result();
    }

    //Test
    public void testWhenWakamitiExceptionWithError() throws Exception {
        // Given
        when(plan.result()).thenThrow(new WakamitiException("Error"));
        when(plan.hasChildren()).thenReturn(true);

        ProjectStub project = new ProjectStub(new File(getBasedir(), "src/test/resources/pom-default.xml"));
        MavenSession session = newMavenSession(project);

        // When
        executeMojo(session, GOAL);

        // Then
        assertThat(session.getResult().getExceptions()).hasSize(1);
        assertThat(session.getResult().getExceptions().get(0)).isOfAnyClassIn(MojoFailureException.class);
        assertThat(session.getResult().getExceptions().get(0)).hasCauseInstanceOf(WakamitiException.class);
        assertThat(session.getResult().getExceptions().get(0)).hasMessage("Wakamiti error: Error");
        verify(plan, times(1)).result();
    }

    //Test
    public void testWhenExceptionWithError() throws Exception {
        // Given
        when(plan.result()).thenThrow(new RuntimeException("Error"));
        when(plan.hasChildren()).thenReturn(true);

        ProjectStub project = new ProjectStub(new File(getBasedir(), "src/test/resources/pom-default.xml"));
        MavenSession session = newMavenSession(project);

        // When
        executeMojo(session, GOAL);

        // Then
        assertThat(session.getResult().getExceptions()).hasSize(1);
        assertThat(session.getResult().getExceptions().get(0)).isOfAnyClassIn(MojoExecutionException.class);
        assertThat(session.getResult().getExceptions().get(0)).hasCauseInstanceOf(RuntimeException.class);
        assertThat(session.getResult().getExceptions().get(0)).hasMessage("Wakamiti configuration error: Error");
        verify(plan, times(1)).result();
    }

    //Test
    public void testWhenNotPassedAndIgnoreWithSuccess() throws Exception {
        // Given
        when(plan.result()).thenReturn(Optional.of(Result.FAILED));
        when(plan.hasChildren()).thenReturn(true);

        ProjectStub project = new ProjectStub(new File(getBasedir(), "src/test/resources/pom-ignore.xml"));
        MavenSession session = newMavenSession(project);

        // When
        executeMojo(session, GOAL);

        // Then
        assertThat(session.getResult().getExceptions()).isEmpty();
        verify(plan, times(1)).result();
    }

    //Test
    public void testWhenWakamitiExceptionAndIgnoreWithError() throws Exception {
        // Given
        when(plan.result()).thenThrow(new WakamitiException("Error"));
        when(plan.hasChildren()).thenReturn(true);

        ProjectStub project = new ProjectStub(new File(getBasedir(), "src/test/resources/pom-ignore.xml"));
        MavenSession session = newMavenSession(project);

        // When
        executeMojo(session, GOAL);

        // Then
        assertThat(session.getResult().getExceptions()).isEmpty();
        verify(plan, times(1)).result();
    }

    //Test
    public void testWhenExceptionAndIgnoreWithError() throws Exception {
        // Given
        when(plan.result()).thenThrow(new RuntimeException("Error"));
        when(plan.hasChildren()).thenReturn(true);

        ProjectStub project = new ProjectStub(new File(getBasedir(), "src/test/resources/pom-ignore.xml"));
        MavenSession session = newMavenSession(project);

        // When
        executeMojo(session, GOAL);

        // Then
        assertThat(session.getResult().getExceptions()).isEmpty();
        verify(plan, times(1)).result();
    }

    //Test
    public void testWhenNoChildrenWithSuccess() throws Exception {
        // Given
        when(plan.hasChildren()).thenReturn(false);

        ProjectStub project = new ProjectStub(new File(getBasedir(), "src/test/resources/pom-default.xml"));
        MavenSession session = newMavenSession(project);

        // When
        executeMojo(session, GOAL);

        // Then
        assertThat(session.getResult().getExceptions()).isEmpty();
        verify(plan, times(0)).result();
    }

}
