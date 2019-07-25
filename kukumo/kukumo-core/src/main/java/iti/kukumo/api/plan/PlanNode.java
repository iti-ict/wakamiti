package iti.kukumo.api.plan;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A test <tt>PlanNode</tt> object is any of the parts that form a test iti.kukumo.test.gherkin.plan
 */
public interface PlanNode {

    /** @return The name of the node */
    String name();
    
    /** @return The text that will be used to show the node in reports or other applications.
     * <p>May be a combination of other fields such as <tt>id</tt>, <tt>name</tt>, <tt>keyword</tt>, etc. */
    String displayName();

    
    /** @return An unambiguous identifier within the iti.kukumo.test.gherkin.plan */
    String id();
    
    /** @return The human language in which the iti.kukumo.test.gherkin.plan node was written */
    String language();
    
    /** @return The type of this node, preferably one from {@link PlanNodeTypes} */
    String nodeType();

    /** @return  A word that has been used to define the node type 
     * (for instance: <tt>Scenario</tt>, <tt>Given</tt>, <tt>When</tt>, <tt>Then</tt>, etc.) */
    String keyword();

    String source();

    /** @return A collection of children nodes, or an empty collection if it is a final node */
    Stream<PlanNode> children();
    
    /** @return The number of children nodes */
    int numChildren();

    boolean isTestCase();


    void clearChildren();
    
	void removeChildrenIf(Predicate<PlanNode> predicate);

    PlanNode child(int index);

    void addChild(PlanNode child);

    void addChildIfSatisfies(PlanNode child, Predicate<PlanNode> filter);

    boolean hasChildren();
    
    boolean containsChild(PlanNode child);

    Optional<String> getTagThatSatisfies(Predicate<String> filter);
    
    List<String> description();
    
    List<String> tags();
    
    Map<String,String> properties();

    
    
    default boolean hasTag(String tag) {
        return tags().contains(tag);
    }

    
    default PlanNodeDescriptor obtainDescriptor() {
    	return new PlanNodeDescriptor(this);
    }
    

    /**
     * Compute the start instant of this node based on the minimum start instant of its children.
     * @return The optional computed start instant (not present if none of the children has been executed)
     */
    default Optional<Instant> computeStartInstant() {
        return children()
                .map(PlanNode::computeStartInstant)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .min(Instant::compareTo);
    }


    /**
     * Compute the finish instant of this node based on the maximum finish instant of its children.
     * @return The optional computed finish instant (not present if none of the children has been executed)
     */
    default Optional<Instant> computeFinishInstant() {
        return children()
                .map(PlanNode::computeFinishInstant)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Instant::compareTo);
    }


    /**
     * Compute the duration of this node based on the difference between the results of {@link #computeStartInstant()}
     * and {@link #computeFinishInstant()}
     * @return The optional compute duration (not present if none of the children has been executed)
     */
    default Optional<Duration> computeDuration() {
    	Optional<Instant> start = computeStartInstant();
    	Optional<Instant> finish = computeFinishInstant();
    	return Optional.ofNullable(start.isPresent() && finish.isPresent() ? 
    			Duration.between(start.get(), finish.get()) : 
    			null);
    }



    /**
     * Compute the execution result of this node based on most relevant results of its children
     * @return The optional computed result (not present if none of the children has been executed)
     */
    default Optional<Result> computeResult() {
    	return children()
                .map(PlanNode::computeResult)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .max(Comparator.naturalOrder());
    }


}
