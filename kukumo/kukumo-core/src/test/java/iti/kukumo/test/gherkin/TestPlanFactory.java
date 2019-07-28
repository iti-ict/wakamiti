package iti.kukumo.test.gherkin;

import iti.commons.configurer.Configuration;
import iti.commons.configurer.ConfigurationException;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.Resource;
import iti.kukumo.api.extensions.Planner;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.api.plan.PlanSerializer;
import iti.kukumo.gherkin.GherkinResourceType;
import iti.kukumo.util.ResourceLoader;
import org.json.JSONException;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;
import org.skyscreamer.jsonassert.comparator.JSONComparator;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class TestPlanFactory {

    private final ResourceLoader resourceLoader = Kukumo.getResourceLoader();
    private final PlanSerializer planSerializer = Kukumo.getPlanSerializer();


    private void assertFilePlan(String filename) throws IOException, JSONException, ConfigurationException {
        String featureFilename = "src/test/resources/features/"+filename+".feature";
        String resultFilename = "src/test/resources/features/"+filename+"_plan.json";
        assertFilePlan(featureFilename,resultFilename,null);
    }


    private void assertFilePlan(String featureFilename, String resultFilename, String tagExpression) 
    throws IOException, JSONException, ConfigurationException {
        List<Resource<?>> resources = resourceLoader.discoverResources(featureFilename, GherkinResourceType.INSTANCE);
        assertPlan(resources, resultFilename, tagExpression);
    }


    private void assertPathPlan(String featurePath, String resultFilename, String tagExpression) 
    throws IOException, JSONException, ConfigurationException {
        List<Resource<?>> resources = resourceLoader.discoverResources(featurePath, GherkinResourceType.INSTANCE);
        assertPlan(resources, resultFilename, tagExpression);
    }

    
    private void assertPlan(List<Resource<?>> gherkinDocuments, String resultFilename, String tagExpression) 
    throws JSONException, IOException, ConfigurationException {
        Properties properties = new Properties();
        if (tagExpression != null) {
            properties.put(KukumoConfiguration.TAG_FILTER, tagExpression);
        }
        Configuration configuration = KukumoConfiguration.defaultConfiguration().appendFromProperties(properties);
        Planner planner = Kukumo.getPlannerFor(GherkinResourceType.INSTANCE).get();
        Kukumo.configure(planner, configuration);
        PlanNode testPlan = planner.createPlan(gherkinDocuments);
        String plan = planSerializer.serialize(testPlan);
        String result = resourceLoader.readResourceAsString(resultFilename);
        JSONComparator comparator = new DefaultComparator(JSONCompareMode.STRICT);
        JSONCompareResult comparison = JSONCompare.compareJSON(result, plan, comparator);
        if (comparison.failed()) {
            throw new ComparisonFailure(comparison.getMessage(), result, plan);
        }
    }




    @Test
    public void test1_simpleScenario() throws IOException, JSONException, ConfigurationException {
        assertFilePlan("test1_simpleScenario");
    }

    @Test
    public void test2_scenarioOutline() throws IOException, JSONException, ConfigurationException {
        assertFilePlan("test2_scenarioOutline");
    }

    @Test
    public void test3_background() throws IOException, JSONException, ConfigurationException {
        assertFilePlan("test3_background");
    }

    @Test
    public void test4_tagExpression() throws IOException, JSONException, ConfigurationException {
        assertFilePlan("src/test/resources/features/test4_tagExpression.feature", "src/test/resources/features/test4_tagExpression_plan.json","Test4 and (B and (C or D))");
    }


    @Test
    public void testRedefining() throws IOException, JSONException, ConfigurationException {
        assertPathPlan("src/test/resources/features/redefining","src/test/resources/features/redefining/redefining_plan.json", null);
    }


}

