package iti.kukumo.report.html;

import static j2html.TagCreator.body;
import static j2html.TagCreator.div;
import static j2html.TagCreator.each;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.head;
import static j2html.TagCreator.html;
import static j2html.TagCreator.li;
import static j2html.TagCreator.meta;
import static j2html.TagCreator.p;
import static j2html.TagCreator.pre;
import static j2html.TagCreator.rawHtml;
import static j2html.TagCreator.script;
import static j2html.TagCreator.span;
import static j2html.TagCreator.style;
import static j2html.TagCreator.table;
import static j2html.TagCreator.thead;
import static j2html.TagCreator.title;
import static j2html.TagCreator.tr;
import static j2html.TagCreator.ul;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;

import iti.commons.jext.Extension;
import iti.kukumo.api.Kukumo;
import iti.kukumo.api.extensions.Reporter;
import iti.kukumo.api.plan.NodeType;
import iti.kukumo.api.plan.PlanNodeDescriptor;
import iti.kukumo.api.plan.Result;
import iti.kukumo.util.KukumoLogger;
import j2html.TagCreator;
import j2html.tags.ContainerTag;
import j2html.tags.DomContent;
import j2html.tags.Tag;

/**
 * @author ITI
 *         Created by ITI on 14/03/19
 */
@Extension(provider = "iti.kukumo", name = "html-report")
public class HtmlReportGenerator implements Reporter {

    private static final String INLINE = "inline";
    private static final String BLOCK = "block";

    private static final Logger LOGGER = KukumoLogger.forClass(HtmlReportGenerator.class);

    private static final int MILLIS_IN_SEC = 1000;
    private static final int MILLIS_IN_MINUTE = 60000;
    private static final int MILLIS_IN_HOUR = 3600000;

    private File outputFile;
    private String cssFile;
    private ResourceBundle resourceBundle;
    private DateTimeFormatter formatter;
    private Map<PlanNodeDescriptor,String> domIDs;



    @Override
    public void report(PlanNodeDescriptor root) {

        LOGGER.info("Generating HTML report...");
        File outputFileFolder = outputFile.getParentFile();
        if (outputFileFolder != null && !outputFileFolder.exists()) {
            outputFileFolder.mkdirs();
        }
        try (FileWriter writer = new FileWriter(outputFile)) {
            generate(root,writer);
            LOGGER.info("Report generated: {uri}", outputFile);
        } catch (IOException e) {
            LOGGER.error("{error} {error}", "Error generating Kukumo HTML report: ", e.getMessage(), e);
        }
    }



    public void setOutputFile(String outputFile) {
        this.outputFile = new File(outputFile);
    }

    public void setReportLocale(Locale reportLocale) {
        this.resourceBundle = Kukumo.instance().getResourceLoader().resourceBundle("messages", reportLocale);
        this.formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL,FormatStyle.MEDIUM).withLocale(reportLocale);
    }

    public void setCssFile(String cssFile) {
        this.cssFile = cssFile;
    }



    public void generate(PlanNodeDescriptor plan, Writer writer) throws IOException {
        this.domIDs = new HashMap<>();
        registerIDs(plan, domIDs, 0);
        ContainerTag html = html(
            head(
              title(msg("title")),
              meta().attr("charset","UTF-8"),
              style( rawHtml( css())  ),
              script( rawHtml( javascript() ))
            ),
            body(
              h1(msg("title")),
              overallResult(plan),
              ul(each(sortByResult(true,emptyIfNull(plan.getChildren())), node->generateNode(node,true)))
            )
        );
         writer.write(html.render());
    }






    private int registerIDs(PlanNodeDescriptor node, Map<PlanNodeDescriptor,String> map, int lastID) {
        lastID++;
        map.put(node,String.valueOf(lastID));
        if (node.getChildren() != null) {
            for (PlanNodeDescriptor child : node.getChildren()) {
                lastID = registerIDs(child, map, lastID);
            }
        }
        return lastID;
    }



    private ContainerTag generateNode(PlanNodeDescriptor node, boolean sortChildren) {
        String domIdChildren = domIDs.get(node)+"_children";
        String domIdExpandIcon = domIDs.get(node)+"_expand";
        String domIdData = domIDs.get(node)+"_data";
        String domIdDesc = domIDs.get(node)+"_desc";
        boolean expandable = (node.getChildren() != null || node.getDataTable() != null || node.getDocument() != null);
        boolean childrenExpanded = (node.getResult() != Result.PASSED);
        boolean sortNextChildren = (sortChildren && (isTestCase(node))) ? false : sortChildren;

        return li(
            div(
            attachSwitchVisibility(div(div(
                span().withClass("icon_"+node.getResult()).withTitle(node.getResult().toString()),
                span(emptyIfNull(node.getId())).withClass("id"),
                span(emptyIfNull(node.getKeyword())).withClasses("keyword","nodeName",node.getResult().toString()),
                span(emptyIfNull(node.getName())).withClasses("nodeName",node.getResult().toString()),
                span()
                        .withClass("expandIcon")
                        .withId(domIdExpandIcon)
                        .withStyle("visibility:"+(expandable?"visible":"hidden")+"; display: "+(childrenExpanded ? "none":INLINE))
                ),
                divStatistics(node, false).withClass("nodeResult"),
                divTags(node)
            )
              .withClasses("node",expandable?"clickable":"")
              .withId(domIDs.get(node))
             ,domIdChildren,BLOCK,domIdExpandIcon,INLINE, domIdData, BLOCK, domIdDesc, BLOCK)
            .withStyle("display:flex;justify-content:space-between")

                    )
                ,
            divDescription(node)
                .withId(domIdDesc)
                .withStyle("display: "+(childrenExpanded ? "block" : "none")),
            divStepExtraData(node),
            divErrorDetails(node),
            ul(each(sortByResult(sortNextChildren,emptyIfNull(node.getChildren())), child->generateNode(child,sortNextChildren)))
                .withId(domIdChildren)
                .withClass((isTestCase(node))?"testCase":"")
                .withStyle("display: "+(childrenExpanded ? "block" : "none"))
        ).withClass((isTestCase(node))?"testCase":"");
    }




    private ContainerTag divStatistics(PlanNodeDescriptor node, boolean includeText) {
        List<Tag<?>> tags = new ArrayList<>();
        if (containsTestCases(node) && !(isTestCase(node))) {
            List<Tag<?>> resultTags = new ArrayList<>();
            Stream.of(Result.values()).sorted(Comparator.reverseOrder()).forEach(result -> {
                Long count = node.getTestCaseResults().get(result);
                if (count != null && count > 0L) {
                    resultTags.add(div(
                            span(""+count).withClass("number"),
                            span( includeText ? msg("testCases."+result) : ""),
                            span().withClasses("icon","icon_"+result)).withClass("resultStatistics")
                    );
                }
            });
            tags.add(div(resultTags.toArray(new ContainerTag[0])).withClass("groupResultStatistics"));
        }
        if (node.getDuration() != null && wasExecuted(node) ) {
            tags.add(span(formatDuration(node.getDuration())).withClass("duration"));
        }
        return div(tags.toArray(new ContainerTag[0])).withStyle("display:flex;align-items: center");
    }




    private boolean containsTestCases(PlanNodeDescriptor node) {
        if ((isTestCase(node)) || node.getChildren() == null) {
            return (isTestCase(node));
        } else {
            return node.getChildren().stream().map(this::containsTestCases).anyMatch(Boolean.TRUE::equals);
        }
    }


    private boolean wasExecuted(PlanNodeDescriptor node) {
        return node.getResult() == Result.PASSED || node.getResult() == Result.FAILED  || node.getResult() == Result.ERROR;
    }


    private ContainerTag divTags(PlanNodeDescriptor node) {
         List<Tag<?>> tags = new ArrayList<>();
         if (node.getTags() != null) {
             node.getTags().sort(Comparator.naturalOrder());
             for (String nodeTag : node.getTags()) {
                 if (nodeTag.equals(node.getId())) {
                     continue;
                 }
                 int tagStyle = Math.abs(nodeTag.hashCode() % 8);
                 tags.add(span(nodeTag).withClass("tag"+tagStyle));
             }
         }
         return div(tags.toArray(new ContainerTag[0])).withClass("tags");
    }

    private Tag<?> divDescription(PlanNodeDescriptor node) {
        boolean hasDescription = (node.getDescription() != null && !node.getDescription().isEmpty());
        String description = "";
        if (hasDescription) {
            description = node.getDescription().stream().collect(Collectors.joining("\n")).replace("\n\n", "<br/>");
        }
        return hasDescription ? p(rawHtml(description)).withClass("description") : div();
    }


    private ContainerTag divErrorDetails(PlanNodeDescriptor node) {
        List<Tag<?>> tags = new ArrayList<>();
        if (node.getResult() == Result.ERROR || node.getResult() == Result.FAILED) {
            boolean trace = (node.getErrorTrace() != null);
            String domIdTrace = domIDs.get(node)+"_errorTrace";
            if (node.getErrorMessage() != null) {
                tags.add(
                        attachSwitchVisibility(
                div(
                        span(node.getErrorMessage()),
                        span().withClass(trace ? "expandIcon":"")
                )
                .withClasses("errorMessage",trace?"clickable":"")
                ,domIdTrace,BLOCK)
                );
            }
            if (trace) {
                tags.add(attachSwitchVisibility(div(pre(node.getErrorTrace())).withStyle("display:none").withClass("errorTrace").withId(domIdTrace),domIdTrace,BLOCK));
            }
        }
        return div(tags.toArray(new ContainerTag[0]));
    }



    private ContainerTag divStepExtraData(PlanNodeDescriptor node) {
        List<Tag<?>> tags = new ArrayList<>();
        String domIdData = domIDs.get(node)+"_data";
        Tag<?> tag = null;
        if (node.getDocument() != null) {
            tag = div(pre(node.getDocument()));
        } else if (node.getDataTable() != null) {
            String[] header = node.getDataTable()[0];
            String[][] body = Arrays.copyOfRange(node.getDataTable(),1,node.getDataTable().length);
            tag = table(thead(foreach(header,TagCreator::td)),foreach(body,row->tr(foreach(row,TagCreator::td))));
        }
        if (tag != null) {
            tags.add(attachSwitchVisibility(tag,domIdData,BLOCK)
                .withStyle("display:none")
                .withClass("document")
                .withId(domIdData));
        }
        return div(tags.toArray(new ContainerTag[0]));
    }





    private Tag<?> overallResult(PlanNodeDescriptor plan) {
        return div(
           h2(
              div(
                span().withClass("icon_"+plan.getResult()),
                span( plan.getResult() == Result.PASSED ? msg("plan.PASSED") : msg("plan.NOT_PASSED"))
              ),
              span(datetime(plan.getStartInstant()))
            ).withClass(plan.getResult().toString()),
           divPercentage(plan),
           divStatistics(plan, true).withClass("overallResult")
        ).withClasses("overallResult","_"+plan.getResult());
    }


    private Tag<?> divPercentage(PlanNodeDescriptor node) {
        float total = node.getTestCaseResults().values().stream().mapToLong(Long::longValue).sum();
        Map<Result, Float> percentages = node.getTestCaseResults().entrySet().stream()
            .collect(Collectors.toMap(Entry<Result, Long>::getKey, e -> e.getValue().floatValue() * 100f / total));
        return div (
        Stream.of(Result.values()).sorted(Comparator.reverseOrder())
                .map(result -> div().withClass("percentage_"+result).withStyle("width: "+percentages.get(result)+"%"))
                .toArray(Tag[]::new)
        ).withClass("percentage");
    }



    private String formatDuration(long timeInMillis) {
        long hours = timeInMillis / MILLIS_IN_HOUR;
        timeInMillis -= hours * MILLIS_IN_HOUR;
        long minutes = timeInMillis / MILLIS_IN_MINUTE;
        timeInMillis -= minutes * MILLIS_IN_MINUTE;
        float seconds = (float)timeInMillis / (float)MILLIS_IN_SEC;
        StringBuilder string = new StringBuilder();
        if (hours > 0) {
            string.append(msg(hours>1?"hours":"hour",hours)).append(" ");
        }
        if (minutes > 0) {
            string.append(msg(minutes>1?"minutes":"minute",minutes)).append(" ");
        } else {
            if (seconds > 1f) {
                seconds = (int)seconds;
            }
            string.append(msg(seconds==1f?"second":"seconds",seconds)).append(" ");
        }
        return string.toString();
    }




    private String css() throws IOException {
        InputStream inputStream =
            cssFile == null ?
            Thread.currentThread().getContextClassLoader().getResourceAsStream("style.css") :
            new FileInputStream(cssFile)
        ;
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
           return reader.lines().collect(Collectors.joining("\n"));
        }
    }


    private String javascript() throws IOException {
        InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("functions.js");
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        }
    }






    private <T> Collection<T> emptyIfNull(Collection<T> collection) {
        return collection == null ? Collections.emptyList() : collection;
    }

    private String emptyIfNull (String string) {
        return string == null ? "" : string;
    }

    private String datetime(String dateTimeISO) {
        return formatter.format(LocalDateTime.parse(dateTimeISO));
    }



    private String msg(String message, Object...args) {
        String translatedMessage = resourceBundle.getString(message);
        if (translatedMessage == null) {
            return message;
        }
        if (args.length == 0) {
            return translatedMessage;
        } else {
            return MessageFormat.format(translatedMessage, args);
        }
    }



    private <T extends Tag<T>> Tag<T> attachSwitchVisibility(Tag<T> tag, String... pairIdsDisplayValues) {
        List<String> ids = new ArrayList<>();
        List<String> displayValues = new ArrayList<>();
        for (int i=0;i<pairIdsDisplayValues.length;i++) {
            if (i % 2 == 0) {
                ids.add(pairIdsDisplayValues[i]);
            } else {
                displayValues.add(pairIdsDisplayValues[i]);
            }
        }
        return attachSwitchVisibility(tag,ids.stream(),displayValues.stream());
    }



    private <T extends Tag<T>> Tag<T> attachSwitchVisibility(Tag<T> tag, Stream<String> ids, Stream<String> displayValues) {
        String strIds = ids.map(s->"'"+s+"'").collect(Collectors.joining(","));
        String strDisplayValues = displayValues.map(s->"'"+s+"'").collect(Collectors.joining(","));
        return tag.attr("onclick","switchVisibility(["+strIds+"],["+strDisplayValues+"])");
    }


    private <T> DomContent foreach(T[] array, Function<? super T,DomContent> function) {
        return TagCreator.each(Arrays.asList(array),function);
    }


    private Collection<PlanNodeDescriptor> sortByResult(boolean sort, Collection<PlanNodeDescriptor> nodes) {
        if (!sort) {
            return nodes;
        }
        return nodes.stream().sorted(Comparator.comparing(PlanNodeDescriptor::getResult).reversed()).collect(Collectors.toList());
    }



    private boolean isTestCase(PlanNodeDescriptor node) {
        return node.getNodeType() == NodeType.TEST_CASE;
    }

}
