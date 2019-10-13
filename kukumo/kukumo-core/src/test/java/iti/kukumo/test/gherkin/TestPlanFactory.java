package iti.kukumo.test.gherkin;

import iti.commons.configurer.Configuration;
import iti.commons.configurer.ConfigurationException;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.KukumoConfiguration;
import iti.kukumo.api.plan.NodeType;
import iti.kukumo.api.plan.PlanNode;
import iti.kukumo.gherkin.GherkinResourceType;
import org.json.JSONException;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.skyscreamer.jsonassert.comparator.DefaultComparator;
import org.skyscreamer.jsonassert.comparator.JSONComparator;

import java.io.IOException;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class TestPlanFactory {

    private void assertFilePlan(String filename, int testCases) throws IOException, JSONException, ConfigurationException {
        String featureFilename = "src/test/resources/features/"+filename+".feature";
        String resultFilename = "src/test/resources/features/"+filename+"_plan.json";
        assertFilePlan(featureFilename,resultFilename,null,testCases);
    }


    private void assertFilePlan(String featureFilename, String resultFilename, String tagExpression, int testCases)
    throws IOException, JSONException, ConfigurationException {
        assertPlan(featureFilename, resultFilename, tagExpression, testCases);
    }


    private void assertPathPlan(String featurePath, String resultFilename, String tagExpression, int testCases)
    throws IOException, JSONException, ConfigurationException {
        assertPlan(featurePath, resultFilename, tagExpression, testCases);
    }


    private void assertPlan(String path, String resultFilename, String tagExpression, int testCases)
    throws JSONException, IOException, ConfigurationException {
        Properties properties = new Properties();
        properties.put(KukumoConfiguration.RESOURCE_TYPES, GherkinResourceType.NAME);
        properties.put(KukumoConfiguration.RESOURCE_PATH, path);
        if (tagExpression != null) {
            properties.put(KukumoConfiguration.TAG_FILTER, tagExpression);
        }
        Configuration configuration = KukumoConfiguration.defaultConfiguration().appendFromProperties(properties);
        Kukumo kukumo = Kukumo.instance();
        PlanNode testPlan = kukumo.createPlanFromConfiguration(configuration);
        System.out.println(printPlan(testPlan,new StringBuilder(),0));

        assertThat(testPlan.numDescendants(NodeType.TEST_CASE)).isEqualTo(testCases);
        String plan = Kukumo.planSerializer().serialize(testPlan);
        String result = Kukumo.resourceLoader().readResourceAsString(resultFilename);
        JSONComparator comparator = new DefaultComparator(JSONCompareMode.STRICT);
        JSONCompareResult comparison = JSONCompare.compareJSON(result, plan, comparator);
        if (comparison.failed()) {
            throw new ComparisonFailure(comparison.getMessage(), result, plan);
        }
    }




    @Test
    public void test1_simpleScenario() throws IOException, JSONException, ConfigurationException {
        assertFilePlan("test1_simpleScenario",1);
    }

    @Test
    public void test2_scenarioOutline() throws IOException, JSONException, ConfigurationException {
        assertFilePlan("test2_scenarioOutline",3);
    }

    @Test
    public void test3_background() throws IOException, JSONException, ConfigurationException {
        assertFilePlan("test3_background",3);
    }

    @Test
    public void test4_tagExpression() throws IOException, JSONException, ConfigurationException {
        assertFilePlan("src/test/resources/features/test4_tagExpression.feature", "src/test/resources/features/test4_tagExpression_plan.json","Test4 and (B and (C or D))",1);
    }


    @Test
    public void testRedefining() throws IOException, JSONException, ConfigurationException {
        assertPathPlan("src/test/resources/features/redefining","src/test/resources/features/redefining/redefining_plan.json", null,4);
    }


    private StringBuilder printPlan(PlanNode node, StringBuilder string, int level) {
        StringBuilder leading = new StringBuilder();
        for (int i=0;i<level;i++) {
            leading.append("--");
        }
        leading.append("  ").append(node.nodeType()).append("  >> ").append(node.displayName());
        string.append(String.format("%-100s %-40s %s\n",
            leading,
            node.tags().isEmpty() ? "" : node.tags().stream().sorted().map(s->"#"+s).collect(Collectors.joining(" ")),
            node.properties().isEmpty() ? "" : node.properties()
        ));
        node.children().forEach(child -> printPlan(child,string,level+1));
        return string;
    }

}

