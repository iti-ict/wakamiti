package es.iti.wakamiti.xray.test;

import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.plan.PlanNodeSnapshot;
import es.iti.wakamiti.api.util.WakamitiLogger;
import es.iti.wakamiti.core.JsonPlanSerializer;
import es.iti.wakamiti.xray.XRaySynchronizer;
import es.iti.wakamiti.xray.internal.Mapper;
import es.iti.wakamiti.xray.model.TestCase;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


public class MapperTest {

    private static final Logger LOGGER = WakamitiLogger.forClass(MapperTest.class);

    private static PlanNodeSnapshot plan;

    @BeforeClass
    public static void setUp() throws IOException {
        plan = new JsonPlanSerializer().read(resource("wakamiti.json"));
    }

    private static InputStream resource(String resource) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
    }

    @Test
    public void testMapTestsWhenFeatureWithSuccess() {
        List<TestCase> tests = Mapper.ofType(XRaySynchronizer.GHERKIN_TYPE_FEATURE).instance(null)
                .map(plan).collect(Collectors.toList());

        assertThat(tests)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .allMatch(tc -> tc.getTestSetList().get(0).getJira().getSummary().equals("features/suite1"))
                .allMatch(tc -> tc.getJira().getSummary().equals("XRay integration feature"));
    }

    @Test
    public void testMapTestsWhenFeatureAndSourceBasedWithSuccess() {
        List<TestCase> tests = Mapper.ofType(XRaySynchronizer.GHERKIN_TYPE_FEATURE).instance("features")
                .map(plan).collect(Collectors.toList());

        assertThat(tests)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .allMatch(tc -> tc.getTestSetList().get(0).getJira().getSummary().equals("suite1"))
                .allMatch(tc -> tc.getJira().getSummary().equals("XRay integration feature"));
    }


    @Test
    public void testMapTestsWhenScenario() {
        List<TestCase> tests = Mapper.ofType(XRaySynchronizer.GHERKIN_TYPE_SCENARIO).instance(null)
                .map(plan).collect(Collectors.toList());

        assertThat(tests.get(0)).hasFieldOrProperty("jira.summary");
        assertThat(tests.get(1)).hasFieldOrProperty("jira.summary");
        assertThat(tests.get(2)).hasFieldOrProperty("jira.summary");
    }

    @Test
    public void testMapTestsWhenScenarioAndSourceBased() {
        List<TestCase> tests = Mapper.ofType(XRaySynchronizer.GHERKIN_TYPE_SCENARIO).instance("features")
                .map(plan).collect(Collectors.toList());

        assertThat(tests.get(0)).hasFieldOrProperty("jira.summary");
        assertThat(tests.get(1)).hasFieldOrProperty("jira.summary");
        assertThat(tests.get(2)).hasFieldOrProperty("jira.summary");
    }

}
