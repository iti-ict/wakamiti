/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.plan;


import es.iti.wakamiti.api.WakamitiException;
import es.iti.wakamiti.api.plan.NodeType;
import es.iti.wakamiti.api.plan.PlanNodeBuilder;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Provides a set of rules and predicates for defining conditions
 * and actions when building a test plan using {@link PlanNodeBuilder}.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public class PlanNodeBuilderRules {

    /**
     * Creates a new {@link Consumer} that performs a binary
     * action on pairs of {@link PlanNodeBuilder}s.
     *
     * @param rightNodeGetter The function to obtain the
     *                        right {@code PlanNodeBuilder}.
     * @param action          The binary action to perform on pairs
     *                        of {@code PlanNodeBuilder}s.
     * @return A new {@code Consumer} that performs the
     * specified binary action on pairs of {@code PlanNodeBuilder}s.
     */
    private static Consumer<PlanNodeBuilder> biconsume(
            Function<PlanNodeBuilder, Optional<PlanNodeBuilder>> rightNodeGetter,
            BiConsumer<PlanNodeBuilder, PlanNodeBuilder> action
    ) {
        return leftNode -> rightNodeGetter.apply(leftNode)
                .ifPresent(rightNode -> action.accept(leftNode, rightNode));
    }

    /**
     * Creates a {@link PlanNodeBuilderRuleConsumer} that applies
     * a set of rules to every descendant {@link PlanNodeBuilder}.
     *
     * @return A new {@code PlanNodeBuilderRuleConsumer} that
     * applies rules to every descendant {@code PlanNodeBuilder}.
     */
    public static PlanNodeBuilderRuleConsumer forEachNode() {
        return forEachNode(it -> true);
    }

    /**
     * Creates a {@link PlanNodeBuilderRuleConsumer} that applies a
     * set of rules to every descendant {@link PlanNodeBuilder}
     * matching the specified predicate.
     *
     * @param predicate The predicate to filter the descendant
     *                  {@code PlanNodeBuilder}s.
     * @return A new {@code PlanNodeBuilderRuleConsumer} that applies
     * rules to every descendant {@code PlanNodeBuilder} matching the
     * specified predicate.
     */
    public static PlanNodeBuilderRuleConsumer forEachNode(Predicate<PlanNodeBuilder> predicate) {
        return new PlanNodeBuilderRuleConsumer(predicate, x -> {
        }, RuleMethod.ALL);
    }

    /**
     * Creates a {@link PlanNodeBuilderRuleConsumer} that applies a
     * set of rules to the first descendant {@link PlanNodeBuilder}
     * matching the specified predicate.
     *
     * @param predicate The predicate to filter the descendant
     *                  {@code PlanNodeBuilder}s.
     * @return A new {@code PlanNodeBuilderRuleConsumer} that applies
     * rules to the first descendant {@code PlanNodeBuilder} matching
     * the specified predicate.
     */
    public static PlanNodeBuilderRuleConsumer forFirstNode(Predicate<PlanNodeBuilder> predicate) {
        return new PlanNodeBuilderRuleConsumer(predicate, x -> {
        }, RuleMethod.FIRST);
    }

    /**
     * Creates a {@link Function} that finds any descendant
     * {@link PlanNodeBuilder} in the root's descendants, satisfying
     * the specified predicate and binary predicate condition.
     *
     * @param predicate   The predicate to filter the descendant {@code PlanNodeBuilder}s.
     * @param biPredicate The binary predicate to test the condition between the left node and the right node.
     * @return A {@code Function} that finds any descendant {@code PlanNodeBuilder} in the root's descendants,
     * satisfying the specified predicate and binary predicate condition.
     */
    public static Function<PlanNodeBuilder, Optional<PlanNodeBuilder>> anyOtherNode(
            Predicate<PlanNodeBuilder> predicate,
            BiPredicate<PlanNodeBuilder, PlanNodeBuilder> biPredicate
    ) {
        return leftNode -> leftNode.root().descendants()
                .filter(predicate)
                .filter(rightNode -> biPredicate.test(leftNode, rightNode))
                .findAny();
    }

    /**
     * Creates a {@link BiPredicate} that tests whether the
     * specified method on the left node is equal to the same
     * method on the right node using {@code Objects.equals}.
     *
     * @param <T>    The type of the method's result.
     * @param method The method to be applied to both nodes for equality comparison.
     * @return A {@code BiPredicate} for testing whether the specified method on the left node
     * is equal to the same method on the right node using {@code Objects.equals}.
     */
    public static <T> BiPredicate<PlanNodeBuilder, PlanNodeBuilder> sharing(
            Function<PlanNodeBuilder, T> method
    ) {
        return (leftNode, rightNode) -> Objects
                .equals(method.apply(leftNode), method.apply(rightNode));
    }

    /**
     * Creates a {@link BiPredicate} that tests whether the
     * specified method on the left node, obtained through a
     * left node getter, is equal to the same method on the
     * right node, obtained through a right node getter, using
     * {@code Objects.equals}.
     *
     * @param <T>             The type of the method's result.
     * @param leftNodeGetter  A function to obtain the left node from the input node.
     * @param rightNodeGetter A function to obtain the right node from the input node.
     * @param method          The method to be applied to both nodes for equality comparison.
     * @return A {@code BiPredicate} for testing whether the specified method on the left node,
     * obtained through a left node getter, is equal to the same method on the right node,
     * obtained through a right node getter, using {@code Objects.equals}.
     */
    public static <T> BiPredicate<PlanNodeBuilder, PlanNodeBuilder> sharing(
            Function<PlanNodeBuilder, Optional<PlanNodeBuilder>> leftNodeGetter,
            Function<PlanNodeBuilder, Optional<PlanNodeBuilder>> rightNodeGetter,
            Function<PlanNodeBuilder, T> method
    ) {
        return (leftNode, rightNode) -> {
            Optional<PlanNodeBuilder> actualLeftNode = leftNodeGetter.apply(leftNode);
            Optional<PlanNodeBuilder> actualRightNode = rightNodeGetter.apply(rightNode);
            return actualLeftNode.isPresent() && actualRightNode.isPresent() &&
                    Objects.equals(method.apply(actualLeftNode.get()), method.apply(actualRightNode.get()));
        };
    }

    /**
     * Creates a {@link Predicate} that tests whether any node,
     * obtained through the specified stream function, matches
     * the given predicate.
     *
     * @param stream    A function to obtain a stream of nodes from the input node.
     * @param predicate The predicate to be tested on each node obtained from the stream.
     * @return A {@code Predicate} for testing whether any node, obtained through the specified stream function,
     * matches the given predicate.
     */
    public static Predicate<PlanNodeBuilder> withAny(
            Function<PlanNodeBuilder, Stream<PlanNodeBuilder>> stream,
            Predicate<PlanNodeBuilder> predicate
    ) {
        return node -> stream.apply(node).anyMatch(predicate);
    }

    /**
     * Creates a {@link Predicate} that tests whether none of
     * the nodes, obtained through the specified stream function,
     * match the given predicate.
     *
     * @param stream    A function to obtain a stream of nodes from the input node.
     * @param predicate The predicate to be tested on each node obtained from the stream.
     * @return A {@code Predicate} for testing whether none of the nodes, obtained through the specified stream function,
     * match the given predicate.
     */
    public static Predicate<PlanNodeBuilder> withNone(
            Function<PlanNodeBuilder, Stream<PlanNodeBuilder>> stream,
            Predicate<PlanNodeBuilder> predicate
    ) {
        return node -> stream.apply(node).noneMatch(predicate);
    }

    /**
     * Creates a {@link Predicate} that tests whether all nodes,
     * obtained through the specified stream function, match the
     * given predicate.
     *
     * @param stream    A function to obtain a stream of nodes from the input node.
     * @param predicate The predicate to be tested on each node obtained from the stream.
     * @return A {@code Predicate} for testing whether all nodes, obtained through the specified stream function,
     * match the given predicate.
     */
    public static Predicate<PlanNodeBuilder> withAll(
            Function<PlanNodeBuilder, Stream<PlanNodeBuilder>> stream,
            Predicate<PlanNodeBuilder> predicate
    ) {
        return node -> stream.apply(node).allMatch(predicate);
    }

    /**
     * Creates a {@link Predicate} that tests whether a
     * specified number of nodes, obtained through the specified
     * stream function, match the given predicate.
     *
     * @param number    A predicate specifying the required number of nodes to match the given predicate.
     * @param stream    A function to obtain a stream of nodes from the input node.
     * @param predicate The predicate to be tested on each node obtained from the stream.
     * @return A {@code Predicate} for testing whether a specified number of nodes, obtained through the specified stream function,
     * match the given predicate.
     */
    public static Predicate<PlanNodeBuilder> withSome(
            LongPredicate number,
            Function<PlanNodeBuilder, Stream<PlanNodeBuilder>> stream,
            Predicate<PlanNodeBuilder> predicate
    ) {
        return node -> number.test(stream.apply(node).filter(predicate).count());
    }

    /**
     * Creates a {@link Predicate} that tests whether a
     * {@link PlanNodeBuilder} has a property with the specified
     * key and value.
     *
     * @param key   The key of the property to check.
     * @param value The value of the property to check.
     * @return A {@code Predicate} for testing whether a {@link PlanNodeBuilder} has a property with the specified key and value.
     */
    public static Predicate<PlanNodeBuilder> withProperty(String key, String value) {
        return node -> value.equals(node.properties().get(key));
    }

    /**
     * Creates a {@link Predicate} that tests whether a
     * {@link PlanNodeBuilder} has a specific tag.
     *
     * @param tag The tag to check for.
     * @return A {@code Predicate} for testing whether a {@link PlanNodeBuilder} has the specified tag.
     */
    public static Predicate<PlanNodeBuilder> withTag(String tag) {
        return node -> node.tags().contains(tag);
    }

    /**
     * Creates a {@link Predicate} that tests whether a
     * {@link PlanNodeBuilder} has a specific node type.
     *
     * @param type The node type to check for.
     * @return A {@code Predicate} for testing whether a {@link PlanNodeBuilder} has the specified node type.
     */
    public static Predicate<PlanNodeBuilder> withType(NodeType type) {
        return node -> node.nodeType() == type;
    }

    /**
     * Creates a {@link Predicate} that tests whether a
     * {@link PlanNodeBuilder} has any of the specified
     * node types.
     *
     * @param types The node types to check for.
     * @return A {@code Predicate} for testing whether a {@link PlanNodeBuilder} has any of the specified node types.
     */
    public static Predicate<PlanNodeBuilder> withTypeAnyOf(NodeType... types) {
        return node -> node.nodeType().isAnyOf(types);
    }

    /**
     * Creates a {@link Predicate} that tests whether a
     * {@link PlanNodeBuilder} has none of the specified
     * node types.
     *
     * @param types The node types to check for.
     * @return A {@code Predicate} for testing whether a {@link PlanNodeBuilder} has none of the specified node types.
     */
    public static Predicate<PlanNodeBuilder> withTypeNoneOf(NodeType... types) {
        return node -> node.nodeType().isNoneOf(types);
    }

    /**
     * Creates a {@link Predicate} that tests whether a
     * {@link PlanNodeBuilder} has a parent satisfying the
     * given predicate.
     *
     * @param predicate The predicate to apply to the parent node.
     * @return A {@code Predicate} for testing whether a {@link PlanNodeBuilder} has a parent satisfying the given predicate.
     */
    public static Predicate<PlanNodeBuilder> withParent(Predicate<PlanNodeBuilder> predicate) {
        return node -> node.parent().filter(predicate).isPresent();
    }

    /**
     * Creates a {@link Predicate} that tests whether
     * a {@link PlanNodeBuilder} has no children.
     *
     * @return A {@code Predicate} for testing whether a {@link PlanNodeBuilder} has no children.
     */
    public static Predicate<PlanNodeBuilder> withoutChildren() {
        return node -> node.numChildren() == 0;
    }

    /**
     * Creates a {@link Predicate} that tests whether
     * a {@link PlanNodeBuilder} has at least one child
     * satisfying the given predicate.
     *
     * @param predicate The predicate to be satisfied by at least one child node.
     * @return A {@code Predicate} for testing whether a {@link PlanNodeBuilder} has at least one child
     * satisfying the given predicate.
     */
    public static Predicate<PlanNodeBuilder> withAnyChild(Predicate<PlanNodeBuilder> predicate) {
        return withAny(PlanNodeBuilder::children, predicate);
    }

    /**
     * Creates a {@link Predicate} that tests whether
     * a {@link PlanNodeBuilder} has no children
     * satisfying the given predicate.
     *
     * @param predicate The predicate to be avoided by all child nodes.
     * @return A {@code Predicate} for testing whether a {@link PlanNodeBuilder} has no children
     * satisfying the given predicate.
     */
    public static Predicate<PlanNodeBuilder> withNoneChild(Predicate<PlanNodeBuilder> predicate) {
        return withNone(PlanNodeBuilder::children, predicate);
    }

    /**
     * Creates a {@link Predicate} that tests whether
     * every child of a {@link PlanNodeBuilder}
     * satisfies the given predicate.
     *
     * @param predicate The predicate to be satisfied by every child node.
     * @return A {@code Predicate} for testing whether every child of a {@link PlanNodeBuilder}
     * satisfies the given predicate.
     */
    public static Predicate<PlanNodeBuilder> withEveryChild(Predicate<PlanNodeBuilder> predicate) {
        return withAll(PlanNodeBuilder::children, predicate);
    }

    /**
     * Creates a {@link Predicate} that tests whether
     * a specific number of children of a {@link PlanNodeBuilder}
     * satisfy the given predicate.
     *
     * @param number    The number of child nodes to satisfy the predicate.
     * @param predicate The predicate to be satisfied by the specified number of child nodes.
     * @return A {@code Predicate} for testing whether a specific number of children of a {@link PlanNodeBuilder}
     * satisfy the given predicate.
     */
    public static Predicate<PlanNodeBuilder> withSomeChildren(
            LongPredicate number,
            Predicate<PlanNodeBuilder> predicate
    ) {
        return withSome(number, PlanNodeBuilder::children, predicate);
    }

    /**
     * Creates a {@link Predicate} that tests
     * whether any descendant of a {@link PlanNodeBuilder}
     * satisfies the given predicate.
     *
     * @param predicate The predicate to be satisfied by at least one descendant node.
     * @return A {@code Predicate} for testing whether any descendant of a {@link PlanNodeBuilder}
     * satisfies the given predicate.
     */
    public static Predicate<PlanNodeBuilder> withAnyDescendant(
            Predicate<PlanNodeBuilder> predicate
    ) {
        return withAny(PlanNodeBuilder::descendants, predicate);
    }

    /**
     * Creates a {@link Predicate} that tests whether
     * none of the descendants of a {@link PlanNodeBuilder}
     * satisfy the given predicate.
     *
     * @param predicate The predicate to be satisfied by none of the descendant nodes.
     * @return A {@code Predicate} for testing whether none of the descendants of a {@link PlanNodeBuilder}
     * satisfy the given predicate.
     */
    public static Predicate<PlanNodeBuilder> withNoneDescendant(
            Predicate<PlanNodeBuilder> predicate
    ) {
        return withNone(PlanNodeBuilder::descendants, predicate);
    }

    /**
     * Creates a {@link Predicate} that tests whether
     * every descendant of a {@link PlanNodeBuilder}
     * satisfies the given predicate.
     *
     * @param predicate The predicate to be satisfied by every descendant node.
     * @return A {@code Predicate} for testing whether every descendant of a {@link PlanNodeBuilder}
     * satisfies the given predicate.
     */
    public static Predicate<PlanNodeBuilder> withEveryDescendant(
            Predicate<PlanNodeBuilder> predicate
    ) {
        return withAll(PlanNodeBuilder::descendants, predicate);
    }

    /**
     * Creates a {@link Predicate} that tests whether
     * a specific number of descendants of a
     * {@link PlanNodeBuilder} satisfy the given predicate.
     *
     * @param number    The required number of descendants that should satisfy the predicate.
     * @param predicate The predicate to be satisfied by some descendants.
     * @return A {@code Predicate} for testing whether a specific number of descendants of a
     * {@link PlanNodeBuilder} satisfy the given predicate.
     */
    public static Predicate<PlanNodeBuilder> withSomeDescendants(
            LongPredicate number,
            Predicate<PlanNodeBuilder> predicate
    ) {
        return withSome(number, PlanNodeBuilder::children, predicate);
    }

    /**
     * Creates a {@link Predicate} that tests whether
     * a {@link PlanNodeBuilder} has at least one ancestor
     * that satisfies the given predicate.
     *
     * @param predicate The predicate to be satisfied by at least one ancestor.
     * @return A {@code Predicate} for testing whether a {@link PlanNodeBuilder} has at least one ancestor
     * that satisfies the given predicate.
     */
    public static Predicate<PlanNodeBuilder> withAnyAncestor(Predicate<PlanNodeBuilder> predicate) {
        return withAny(PlanNodeBuilder::ancestors, predicate);
    }

    /**
     * Creates a {@link Predicate} that tests whether
     * a {@link PlanNodeBuilder} has no ancestors
     * satisfying the given predicate.
     *
     * @param predicate The predicate to be satisfied by none of the ancestors.
     * @return A {@code Predicate} for testing whether a {@link PlanNodeBuilder} has no ancestors
     * satisfying the given predicate.
     */
    public static Predicate<PlanNodeBuilder> withNoneAncestor(Predicate<PlanNodeBuilder> predicate) {
        return withNone(PlanNodeBuilder::ancestors, predicate);
    }

    /**
     * Creates a {@link Predicate} that tests whether
     * every ancestor of a {@link PlanNodeBuilder}
     * satisfies the given predicate.
     *
     * @param predicate The predicate to be satisfied by every ancestor.
     * @return A {@code Predicate} for testing whether every ancestor of a {@link PlanNodeBuilder}
     * satisfies the given predicate.
     */
    public static Predicate<PlanNodeBuilder> withEveryAncestor(
            Predicate<PlanNodeBuilder> predicate
    ) {
        return withAll(PlanNodeBuilder::ancestors, predicate);
    }

    /**
     * Creates a {@link Predicate} that tests whether
     * a specific number of ancestors of a {@link PlanNodeBuilder}
     * satisfy the given predicate.
     *
     * @param number    The specific number of ancestors to satisfy the predicate.
     * @param predicate The predicate to be satisfied by some ancestors.
     * @return A {@code Predicate} for testing whether a specific number of ancestors of a {@link PlanNodeBuilder}
     * satisfy the given predicate.
     */
    public static Predicate<PlanNodeBuilder> withSomeAncestors(
            LongPredicate number,
            Predicate<PlanNodeBuilder> predicate
    ) {
        return withSome(number, PlanNodeBuilder::ancestors, predicate);
    }

    /**
     * Creates a {@link Predicate} that tests whether
     * at least one sibling of a {@link PlanNodeBuilder}
     * satisfies the given predicate.
     *
     * @param predicate The predicate to be satisfied by any sibling.
     * @return A {@code Predicate} for testing whether at least one sibling of a {@link PlanNodeBuilder}
     * satisfies the given predicate.
     */
    public static Predicate<PlanNodeBuilder> withAnySibling(Predicate<PlanNodeBuilder> predicate) {
        return withAny(PlanNodeBuilder::siblings, predicate);
    }

    /**
     * Creates a {@link Predicate} that tests whether
     * none of the siblings of a {@link PlanNodeBuilder}
     * satisfy the given predicate.
     *
     * @param predicate The predicate to be satisfied by none of the siblings.
     * @return A {@code Predicate} for testing whether none of the siblings of a {@link PlanNodeBuilder}
     * satisfy the given predicate.
     */
    public static Predicate<PlanNodeBuilder> withNoneSibling(Predicate<PlanNodeBuilder> predicate) {
        return withNone(PlanNodeBuilder::siblings, predicate);
    }

    /**
     * Creates a {@link Predicate} that tests whether
     * every sibling of a {@link PlanNodeBuilder}
     * satisfies the given predicate.
     *
     * @param predicate The predicate to be satisfied by every sibling.
     * @return A {@code Predicate} for testing whether every sibling of a {@link PlanNodeBuilder}
     * satisfies the given predicate.
     */
    public static Predicate<PlanNodeBuilder> withEverySibling(
            Predicate<PlanNodeBuilder> predicate
    ) {
        return withAll(PlanNodeBuilder::siblings, predicate);
    }

    /**
     * Creates a {@link Predicate} that tests whether
     * a specific number of siblings of a {@link PlanNodeBuilder}
     * satisfy the given predicate.
     *
     * @param number    The number of siblings that should satisfy the predicate.
     * @param predicate The predicate to be satisfied by some siblings.
     * @return A {@code Predicate} for testing whether a specific number of siblings of a {@link PlanNodeBuilder}
     * satisfy the given predicate.
     */
    public static Predicate<PlanNodeBuilder> withSomeSiblings(
            LongPredicate number,
            Predicate<PlanNodeBuilder> predicate
    ) {
        return withSome(number, PlanNodeBuilder::siblings, predicate);
    }

    /**
     * Returns the provided predicate unchanged. This
     * method is a convenience function that can be used
     * to create a {@link Predicate<PlanNodeBuilder>}
     * representing a condition for any node that satisfies
     * the given predicate.
     *
     * @param predicate The predicate representing a condition for any node.
     * @return The provided predicate unchanged.
     */
    public static Predicate<PlanNodeBuilder> anyNode(Predicate<PlanNodeBuilder> predicate) {
        return predicate;
    }

    /**
     * Returns the negation of the provided predicate. This
     * method is a convenience function that can be used
     * to create a {@link Predicate<PlanNodeBuilder>}
     * representing a condition for no node that satisfies
     * the given predicate.
     *
     * @param predicate The predicate representing a condition for nodes.
     * @return The negation of the provided predicate.
     */
    public static Predicate<PlanNodeBuilder> noneNode(Predicate<PlanNodeBuilder> predicate) {
        return predicate.negate();
    }

    /**
     * Returns a predicate that tests whether a node is
     * a child of another node that satisfies the given
     * predicate.
     * This method is useful for creating conditions based
     * on the parent-child relationship in a tree structure.
     *
     * @param predicate The predicate representing a condition for the parent node.
     * @return A predicate testing whether a node is a child of another node that satisfies the given predicate.
     */
    public static Predicate<PlanNodeBuilder> childOf(Predicate<PlanNodeBuilder> predicate) {
        return node -> node.parent().map(predicate::test).orElse(false);
    }

    /**
     * Returns a consumer that removes a node from its parent.
     * If the node has no parent, no action is taken.
     *
     * @return A consumer that removes a node from its parent if it has one.
     */
    public static Consumer<PlanNodeBuilder> removeNode() {
        return node -> node.parent().ifPresent(parent -> parent.removeChild(node));
    }

    /**
     * Returns a bi-consumer that copies properties from
     * the right node to the left node.
     *
     * @return A bi-consumer that copies properties from
     * the right node to the left node.
     */
    public static BiConsumer<PlanNodeBuilder, PlanNodeBuilder> copyProperties() {
        return (leftNode, rightNode) -> leftNode.addProperties(rightNode.properties());
    }

    private enum RuleMethod {
        ALL((list, consumer) -> list.forEach(consumer)),
        FIRST((list, consumer) -> list.stream().findAny().ifPresent(consumer));

        private final BiConsumer<List<PlanNodeBuilder>, Consumer<PlanNodeBuilder>> consumer;


        RuleMethod(BiConsumer<List<PlanNodeBuilder>, Consumer<PlanNodeBuilder>> consumer) {
            this.consumer = consumer;
        }


        public void apply(List<PlanNodeBuilder> list, Consumer<PlanNodeBuilder> consumer) {
            this.consumer.accept(list, consumer);
        }
    }

    /**
     * Interface representing a rule to be applied to a
     * {@link PlanNodeBuilder}.
     */
    public interface PlanNodeBuilderRule {

        void apply(PlanNodeBuilder node) throws WakamitiException;
    }

    /**
     * Implementation of a rule that performs an action
     * on a {@link PlanNodeBuilder}.
     */
    public static class PlanNodeBuilderRuleConsumer implements PlanNodeBuilderRule {

        protected final Predicate<PlanNodeBuilder> predicate;
        protected final Consumer<PlanNodeBuilder> consumer;
        protected final RuleMethod method;


        private PlanNodeBuilderRuleConsumer(
                Predicate<PlanNodeBuilder> predicate,
                Consumer<PlanNodeBuilder> consumer,
                RuleMethod method
        ) {
            this.predicate = predicate;
            this.consumer = consumer;
            this.method = method;
        }

        /**
         * Specify an action to be performed on the matching {@code PlanNodeBuilder}.
         *
         * @param action The action to perform.
         * @return A new {@code PlanNodeBuilderRuleConsumer} with the specified action.
         */
        public PlanNodeBuilderRuleConsumer perform(Consumer<PlanNodeBuilder> action) {
            return new PlanNodeBuilderRuleConsumer(predicate, action, method);
        }

        /**
         * Assert a condition for the matching {@code PlanNodeBuilder}, throwing an exception if the
         * assertion fails.
         *
         * @param assertion       The condition to assert.
         * @param messageSupplier A function providing an error message if the assertion fails.
         * @return A new {@code PlanNodeBuilderRuleConsumer} with the specified assertion.
         * @throws WakamitiException if the assertion fails.
         */
        public PlanNodeBuilderRuleConsumer assertThat(
                Predicate<PlanNodeBuilder> assertion,
                Function<PlanNodeBuilder, String> messageSupplier
        ) {
            return new PlanNodeBuilderRuleConsumer(predicate, node -> {
                if (!assertion.test(node)) {
                    throw new WakamitiException(messageSupplier.apply(node));
                }
            }, method);
        }

        /**
         * Apply the rule to the provided {@code PlanNodeBuilder}.
         *
         * @param plan The {@code PlanNodeBuilder} to apply the rule to.
         */
        @Override
        public void apply(PlanNodeBuilder plan) {
            List<PlanNodeBuilder> nodes = plan
                    .descendants()
                    .filter(predicate)
                    .collect(Collectors.toList());
            method.apply(nodes, consumer);
        }

        /**
         * Specify a binary action to be performed on the
         * matching {@code PlanNodeBuilder} and another
         * {@code PlanNodeBuilder} obtained from a given
         * function.
         *
         * @param rightNodeGetter A function providing the
         *                        second {@code PlanNodeBuilder}.
         * @return A new {@code PlanNodeBuilderRuleBiConsumer}
         * with the specified binary action.
         */
        public PlanNodeBuilderRuleBiConsumer given(
                Function<PlanNodeBuilder, Optional<PlanNodeBuilder>> rightNodeGetter
        ) {
            return new PlanNodeBuilderRuleBiConsumer(predicate, consumer, method, rightNodeGetter);
        }
    }

    /**
     * Implementation of a rule that performs a binary action
     * on two {@link PlanNodeBuilder} instances.
     */
    public static class PlanNodeBuilderRuleBiConsumer extends PlanNodeBuilderRuleConsumer {

        protected final Function<PlanNodeBuilder, Optional<PlanNodeBuilder>> rightNodeGetter;


        private PlanNodeBuilderRuleBiConsumer(
                Predicate<PlanNodeBuilder> predicate,
                Consumer<PlanNodeBuilder> consumer,
                RuleMethod method,
                Function<PlanNodeBuilder, Optional<PlanNodeBuilder>> rightNodeGetter
        ) {
            super(predicate, consumer, method);
            this.rightNodeGetter = rightNodeGetter;
        }

        /**
         * Specify a binary action to be performed on pairs
         * of matching {@code PlanNodeBuilder}s.
         *
         * @param action The binary action to perform on pairs
         *               of {@code PlanNodeBuilder}s.
         * @return A new {@code PlanNodeBuilderRuleBiConsumer}
         * with the specified binary action.
         */
        public PlanNodeBuilderRuleBiConsumer perform(
                BiConsumer<PlanNodeBuilder, PlanNodeBuilder> action
        ) {
            return new PlanNodeBuilderRuleBiConsumer(
                    predicate, biconsume(rightNodeGetter, action), method, rightNodeGetter
            );
        }

    }

}