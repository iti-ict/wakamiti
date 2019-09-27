package iti.kukumo.api.plan;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Stream;

/**
 * A <tt>PlanNode</tt> object is any of the parts that form a test plan
 */
public interface PlanNode {

    /** @return The name of the node */
    String name();


    /** @return The text that will be used to show the node in reports or other applications.
     * <p>May be a combination of other fields such as <tt>id</tt>, <tt>name</tt>, <tt>keyword</tt>, etc. */
    String displayName();


    /** @return An unambiguous identifier within the plan. Can be <tt>null</tt>. */
    String id();


    /** @return The human language in which the node was written */
    String language();


    /** @return The type of this node */
    NodeType nodeType();


    /**
     * @return  A word that has been used to define the node type.
     * (for instance: <tt>Scenario</tt>, <tt>Given</tt>, <tt>When</tt>, <tt>Then</tt>, etc.).
     * It is not relevant to the semantics of the node.
     * */
    String keyword();


    /** @return The source where the node is defined at (for example, a line of a file) */
    String source();


    /** @return A collection of children nodes, or an empty collection if it is a final node */
    Stream<PlanNode> children();


    /** @return The number of children nodes */
    int numChildren();


    /**
     * @return The child node in the given position
     * @throws  IndexOutOfBoundsException if there is no such children position
     * */
    PlanNode child(int index);


    /** @return Whether the node has any children */
    boolean hasChildren();


    /** @return A non-null list of lines of description for the node */
    List<String> description();


    /** @return A non-null set of tags for the node */
    Set<String> tags();


    /** @return A non-null map of properties in the form <tt>key:value</tt> */
    Map<String,String> properties();


    /** @return Whether the node is tagged with the given tag */
    boolean hasTag(String tag);


    /** @return An immutable descriptor of the current state of the node */
    PlanNodeDescriptor obtainDescriptor();


    /** @return An optional string document defined in the node (usually only existing in steps) */
    Optional<Document> document();


    /** @return An optional data table defined in the node (usually only existing in steps) */
    Optional<DataTable> dataTable();


    /**
     * Prepare the node to be executed (usually only called for steps).
     * @return The node execution details
     */
    PlanNodeExecution prepareExecution();


    /** @return The execution details of the node. It will be
     * empty until {@link #prepareExecution()} is called. */
    Optional<PlanNodeExecution> execution();


    /**
     * Get the start instant of this node, if executed.
     * In the case of child-populated nodes, return the minimum start instant of its children.
     * @return The nullable optional start instant
     */
    default Optional<Instant> startInstant() {
        if (hasChildren()) {
            return children()
                .map(PlanNode::startInstant)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .min(Comparator.naturalOrder())
                ;
        }
        return execution().flatMap(PlanNodeExecution::startInstant);
    }


    /**
     * Get the start instant of this node, if executed.
     * In the case of child-populated nodes, return the minimum start instant of its children.
     * @return The nullable optional finish instant
     */
    default Optional<Instant> finishInstant() {
         if (hasChildren()) {
             return children()
                 .map(PlanNode::finishInstant)
                 .filter(Optional::isPresent)
                 .map(Optional::get)
                 .max(Comparator.naturalOrder())
                 ;
         }
         return execution().flatMap(PlanNodeExecution::finishInstant);
    }


    /**
     * Get the duration of the execution of this node, if executed.
     * In the case of child-populated nodes, return the minimum start instant of its children.
     * @return The nullable optional finish instant
     */
    default Optional<Duration> duration() {
        if (hasChildren()) {
            return children()
                .map(PlanNode::duration)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Comparator.naturalOrder())
                ;
        }
        return execution().flatMap(PlanNodeExecution::duration);
    }


    /**
     * Get the result of this node, if executed.
     * In the case of child-populated nodes, return the result of max priority of its children.
     * @return The nullable optional result
     */
    default Optional<Result> result() {
        if (hasChildren()) {
            return children()
                .map(PlanNode::result)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Comparator.naturalOrder())
                ;
        }
        return execution().flatMap(PlanNodeExecution::result);
    }


    /**
     * Get a stream with the errors of this node, if executed and failed.
     * In the case of child-populated nodes, return all the errors of its children.
     * @return A stream of errors
     */
    default Stream<Throwable> errors() {
        if (hasChildren()) {
            return children().flatMap(PlanNode::errors);
        }
        return execution().flatMap(PlanNodeExecution::error).map(Stream::of).orElse(Stream.empty());
    }


     /**
     * @return Check whether the execution of this node has been marked as started.
     * In the case of child-populated nodes, return if every child has been started.
     */
    default boolean hasStarted() {
        if (hasChildren()) {
            return children().allMatch(PlanNode::hasStarted);
        }
        return execution().map(PlanNodeExecution::hasStarted).orElse(false);
    }



    /**
     * @return Check whether the execution of this node has been marked as finished.
     * In the case of child-populated nodes, return if every child has been finished.
     */
    default boolean hasFinished() {
        if (hasChildren()) {
            return children().allMatch(PlanNode::hasFinished);
        }
        return execution().map(PlanNodeExecution::hasFinished).orElse(false);
    }



    default Stream<PlanNode> descendants() {
        return Stream.concat(children(), children().flatMap(PlanNode::descendants));
    }


    /**
     * @return The number of child nodes of the given type
     */
    default int numDescendants(NodeType nodeType) {
        return (int) descendants()
               .map(PlanNode::nodeType)
               .filter(childType -> childType == nodeType)
               .count();
    }


    /**
     * @return The number of child nodes of the given type and the given execution result
     */
    default int numDescendants(NodeType nodeType, Result result) {
        return (int) descendants()
                .filter(child -> child.execution().isPresent())
                .filter(child -> child.execution().get().result().isPresent())
                .filter(child -> child.execution().get().result().get() == result)
                .filter(child -> child.nodeType() == nodeType)
                .count();
    }

}
