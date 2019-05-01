package iti.kukumo.api.plan;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;


@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Setter(AccessLevel.PACKAGE)
@Getter(AccessLevel.PUBLIC)
/**
 * This class is a immutable, non-executable representation of a {@link PlanNode} in a specific state.
 * <p>
 * It is mainly used for serialization/deserialization operations.
 * </p>
 */
public class PlanNodeDescriptor {

     
    private String nodeType;
    private String id;
    private String name;
    private String keyword;
    private String language;
    private String source;
    private String displayName;
    private boolean testCase;
    private List<String> description;
    private List<String> tags;
    private Map<String,String> properties;
    private String startInstant;
    private String finishInstant;
    private Long duration;
    private String document;
    private String documentType;
    private String[][] dataTable;
    private String errorMessage;
    private String errorTrace;
    private Result result;
    private Map<Result,Long> testCaseResults;
    private List<PlanNodeDescriptor> children;


    PlanNodeDescriptor(PlanNode node) {
        this.nodeType = node.nodeType();
        this.id = node.id();
        this.name = node.name();
        this.keyword = node.keyword();
        this.language = node.language();
        this.source = node.source();
        this.displayName = node.displayName();
        this.testCase = node.isTestCase();
        this.description = (node.description() == null ? null : new ArrayList<>(node.description()));
        this.tags = (node.tags() == null ? null : new ArrayList<>(node.tags()));
        this.properties = (node.properties() == null? null : new LinkedHashMap<>(node.properties()));
        this.startInstant = node.computeStartInstant().map(this::instantToString).orElse(null);
        this.finishInstant = node.computeFinishInstant().map(this::instantToString).orElse(null);
        this.duration = node.computeDuration().map(Duration::toMillis).orElse(null);
        this.result = node.computeResult().orElse(null);
        if (node instanceof PlanStep) {
            PlanStep step = (PlanStep) node;
            this.document = step.getDocument().map(Document::getContent).orElse(null);
            this.documentType = step.getDocument().map(Document::getContentType).orElse(null);
            this.dataTable = step.getDataTable().map(DataTable::getValues).orElse(null);
            this.errorMessage = step.getError().map(Throwable::getLocalizedMessage).orElse(null);
            this.errorTrace = step.getError().map(this::errorTrace).orElse(null);
        }
        if (node.hasChildren()) {
            this.children = node.children().map(PlanNodeDescriptor::new).collect(Collectors.toList());
            this.testCaseResults = countTestCases(node);
        }

    }




    private String instantToString(Instant instant) {
        return LocalDateTime.ofInstant(instant,ZoneOffset.UTC).toString();
    }

    private String errorTrace(Throwable error) {
        StringWriter errorWriter = new StringWriter();
        error.printStackTrace(new PrintWriter(errorWriter));
        return errorWriter.toString();
    }
    
    
    
    
    /**
     * Creates a new node descriptor as a parent of the specified nodes
     * @param nodes The nodes to be grouped
     * @return A new parent node descriptor
     */
    public static PlanNodeDescriptor group(PlanNodeDescriptor... nodes) {
        if (nodes.length == 1) {
            return nodes[0];
        }
        PlanNodeDescriptor root = new PlanNodeDescriptor();
        root.children = Arrays.asList(nodes);
        root.startInstant = childLocalDateTime(root,PlanNodeDescriptor::getStartInstant,(x,y)->x.compareTo(y)<0?x:y);
        root.finishInstant = childLocalDateTime(root,PlanNodeDescriptor::getFinishInstant,(x,y)->x.compareTo(y)>0?x:y);
        root.duration = maxChild(root,PlanNodeDescriptor::getDuration);
        root.result = maxChild(root,PlanNodeDescriptor::getResult);
        root.testCaseResults = new LinkedHashMap<>();
        root.children.stream().map(PlanNodeDescriptor::getTestCaseResults).filter(Objects::nonNull)
        .flatMap(map->map.entrySet().stream()).forEach(entry -> {
            root.testCaseResults.computeIfAbsent(entry.getKey(), x->0L);
            root.testCaseResults.put(entry.getKey(), root.testCaseResults.get(entry.getKey())+entry.getValue());
        });
        return root;
    }
    
      
    private static  Map<Result,Long> countTestCases(PlanNode node) {
        LinkedHashMap<Result,Long> results = new LinkedHashMap<>();
        if (node.isTestCase()) {
            node.computeResult().ifPresent(testCaseResult -> results.put(testCaseResult, 1L));
        } else if (node.hasChildren()){
            node.children().map(PlanNodeDescriptor::countTestCases).filter(Objects::nonNull).flatMap(map->map.entrySet().stream())
            .forEach(entry ->{
               results.computeIfAbsent(entry.getKey(), x->0L);
               results.put(entry.getKey(), results.get(entry.getKey())+entry.getValue());
            });
        }
        return results;
    }

    
    private static String childLocalDateTime(PlanNodeDescriptor node, 
            Function<PlanNodeDescriptor,String> method, BinaryOperator<LocalDateTime> reductor
    ) {
        return node.children.stream().map(method).filter(Objects::nonNull).map(LocalDateTime::parse).reduce(reductor)
            .map(LocalDateTime::toString).orElse(null);
    }
    
    private static <T extends Comparable<T>> T maxChild (PlanNodeDescriptor node, Function<PlanNodeDescriptor,T> mapper) {
        return node.children.stream().map(mapper).filter(Objects::nonNull).max(Comparator.naturalOrder()).orElse(null);
    }
    
 
}
