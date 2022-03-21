/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package iti.kukumo.core.plan;


import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import iti.kukumo.api.KukumoException;
import iti.kukumo.api.plan.NodeType;
import iti.kukumo.api.plan.PlanNodeBuilder;


public class PlanNodeBuilderRules {

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


    public interface PlanNodeBuilderRule {

        void apply(PlanNodeBuilder node) throws KukumoException;
    }


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


        public PlanNodeBuilderRuleConsumer perform(Consumer<PlanNodeBuilder> action) {
            return new PlanNodeBuilderRuleConsumer(predicate, action, method);
        }


        public PlanNodeBuilderRuleConsumer assertThat(
            Predicate<PlanNodeBuilder> assertion,
            Function<PlanNodeBuilder, String> messageSuplier
        ) {
            return new PlanNodeBuilderRuleConsumer(predicate, node -> {
                if (!assertion.test(node)) {
                    throw new KukumoException(messageSuplier.apply(node));
                }
            }, method);
        }


        @Override
        public void apply(PlanNodeBuilder plan) {
            List<PlanNodeBuilder> nodes = plan
                .descendants()
                .filter(predicate)
                .collect(Collectors.toList());
            method.apply(nodes, consumer);
        }


        public PlanNodeBuilderRuleBiConsumer given(
            Function<PlanNodeBuilder, Optional<PlanNodeBuilder>> rightNodeGetter
        ) {
            return new PlanNodeBuilderRuleBiConsumer(predicate, consumer, method, rightNodeGetter);
        }
    }


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


        public PlanNodeBuilderRuleBiConsumer perform(
            BiConsumer<PlanNodeBuilder, PlanNodeBuilder> action
        ) {
            return new PlanNodeBuilderRuleBiConsumer(
                predicate, biconsume(rightNodeGetter, action), method, rightNodeGetter
            );
        }

    }


    private static Consumer<PlanNodeBuilder> biconsume(
        Function<PlanNodeBuilder, Optional<PlanNodeBuilder>> rightNodeGetter,
        BiConsumer<PlanNodeBuilder, PlanNodeBuilder> action
    ) {
        return leftNode -> rightNodeGetter.apply(leftNode)
            .ifPresent(rightNode -> action.accept(leftNode, rightNode));
    }

    public static PlanNodeBuilderRuleConsumer forEachNode() {
        return forEachNode(it->true);
    }


    public static PlanNodeBuilderRuleConsumer forEachNode(Predicate<PlanNodeBuilder> predicate) {
        return new PlanNodeBuilderRuleConsumer(predicate, x -> {
        }, RuleMethod.ALL);
    }


    public static PlanNodeBuilderRuleConsumer forFirstNode(Predicate<PlanNodeBuilder> predicate) {
        return new PlanNodeBuilderRuleConsumer(predicate, x -> {
        }, RuleMethod.FIRST);
    }


    public static Function<PlanNodeBuilder, Optional<PlanNodeBuilder>> anyOtherNode(
        Predicate<PlanNodeBuilder> predicate,
        BiPredicate<PlanNodeBuilder, PlanNodeBuilder> biPredicate
    ) {
        return leftNode -> leftNode.root().descendants()
            .filter(predicate)
            .filter(rightNode -> biPredicate.test(leftNode, rightNode))
            .findAny();
    }


    public static <T> BiPredicate<PlanNodeBuilder, PlanNodeBuilder> sharing(
        Function<PlanNodeBuilder, T> method
    ) {
        return (leftNode, rightNode) -> Objects
            .equals(method.apply(leftNode), method.apply(rightNode));
    }


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


    public static Predicate<PlanNodeBuilder> withAny(
        Function<PlanNodeBuilder, Stream<PlanNodeBuilder>> stream,
        Predicate<PlanNodeBuilder> predicate
    ) {
        return node -> stream.apply(node).anyMatch(predicate);
    }


    public static Predicate<PlanNodeBuilder> withAll(
        Function<PlanNodeBuilder, Stream<PlanNodeBuilder>> stream,
        Predicate<PlanNodeBuilder> predicate
    ) {
        return node -> stream.apply(node).allMatch(predicate);
    }


    public static Predicate<PlanNodeBuilder> withSome(
        LongPredicate number,
        Function<PlanNodeBuilder, Stream<PlanNodeBuilder>> stream,
        Predicate<PlanNodeBuilder> predicate
    ) {
        return node -> number.test(stream.apply(node).filter(predicate).count());
    }


    public static Predicate<PlanNodeBuilder> withProperty(String key, String value) {
        return node -> value.equals(node.properties().get(key));
    }


    public static Predicate<PlanNodeBuilder> withTag(String tag) {
        return node -> node.tags().contains(tag);
    }


    public static Predicate<PlanNodeBuilder> withType(NodeType type) {
        return node -> node.nodeType() == type;
    }


    public static Predicate<PlanNodeBuilder> withTypeAnyOf(NodeType... types) {
        return node -> node.nodeType().isAnyOf(types);
    }


    public static Predicate<PlanNodeBuilder> withTypeNoneOf(NodeType... types) {
        return node -> node.nodeType().isNoneOf(types);
    }


    public static Predicate<PlanNodeBuilder> withParent(Predicate<PlanNodeBuilder> predicate) {
        return node -> node.parent().filter(predicate).isPresent();
    }


    public static Predicate<PlanNodeBuilder> withoutChildren() {
        return node -> node.numChildren() == 0;
    }


    public static Predicate<PlanNodeBuilder> withAnyChild(Predicate<PlanNodeBuilder> predicate) {
        return withAny(PlanNodeBuilder::children, predicate);
    }


    public static Predicate<PlanNodeBuilder> withEveryChild(Predicate<PlanNodeBuilder> predicate) {
        return withAll(PlanNodeBuilder::children, predicate);
    }


    public static Predicate<PlanNodeBuilder> withSomeChildren(
        LongPredicate number,
        Predicate<PlanNodeBuilder> predicate
    ) {
        return withSome(number, PlanNodeBuilder::children, predicate);
    }


    public static Predicate<PlanNodeBuilder> withAnyDescendant(
        Predicate<PlanNodeBuilder> predicate
    ) {
        return withAny(PlanNodeBuilder::descendants, predicate);
    }


    public static Predicate<PlanNodeBuilder> withEveryDescendant(
        Predicate<PlanNodeBuilder> predicate
    ) {
        return withAll(PlanNodeBuilder::descendants, predicate);
    }


    public static Predicate<PlanNodeBuilder> withSomeDescendants(
        LongPredicate number,
        Predicate<PlanNodeBuilder> predicate
    ) {
        return withSome(number, PlanNodeBuilder::children, predicate);
    }


    public static Predicate<PlanNodeBuilder> withAnyAncestor(Predicate<PlanNodeBuilder> predicate) {
        return withAny(PlanNodeBuilder::ancestors, predicate);
    }


    public static Predicate<PlanNodeBuilder> withEveryAncestor(
        Predicate<PlanNodeBuilder> predicate
    ) {
        return withAll(PlanNodeBuilder::ancestors, predicate);
    }


    public static Predicate<PlanNodeBuilder> withSomeAncestors(
        LongPredicate number,
        Predicate<PlanNodeBuilder> predicate
    ) {
        return withSome(number, PlanNodeBuilder::ancestors, predicate);
    }


    public static Predicate<PlanNodeBuilder> withAnySibling(Predicate<PlanNodeBuilder> predicate) {
        return withAny(PlanNodeBuilder::siblings, predicate);
    }


    public static Predicate<PlanNodeBuilder> withEverySibling(
        Predicate<PlanNodeBuilder> predicate
    ) {
        return withAll(PlanNodeBuilder::siblings, predicate);
    }


    public static Predicate<PlanNodeBuilder> withSomeSiblings(
        LongPredicate number,
        Predicate<PlanNodeBuilder> predicate
    ) {
        return withSome(number, PlanNodeBuilder::siblings, predicate);
    }


    public static Predicate<PlanNodeBuilder> anyNode(Predicate<PlanNodeBuilder> predicate) {
        return predicate;
    }


    public static Predicate<PlanNodeBuilder> childOf(Predicate<PlanNodeBuilder> predicate) {
        return node -> node.parent().map(predicate::test).orElse(false);
    }


    public static Consumer<PlanNodeBuilder> removeNode() {
        return node -> node.parent().ifPresent(parent -> parent.removeChild(node));
    }


    public static BiConsumer<PlanNodeBuilder, PlanNodeBuilder> copyProperties() {
        return (leftNode, rightNode) -> leftNode.addProperties(rightNode.properties());
    }

}