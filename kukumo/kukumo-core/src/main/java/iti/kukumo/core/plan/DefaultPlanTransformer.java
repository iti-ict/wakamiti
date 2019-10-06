package iti.kukumo.core.plan;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import iti.kukumo.api.plan.PlanNodeBuilder;

public class DefaultPlanTransformer {



    public interface PlanNodeBuilderPredicate extends Predicate<PlanNodeBuilder> {

        static PlanNodeBuilderPredicate forAnyNode(PlanNodeBuilderPredicate predicate) {
            return predicate;
        }

        default PlanNodeBuilderPredicate and(PlanNodeBuilderPredicate predicate) {
            return node -> this.test(node) && predicate.test(node);
        }

        default PlanNodeBuilderPredicate or(PlanNodeBuilderPredicate predicate) {
            return node -> this.test(node) || predicate.test(node);
        }


        default PlanNodeBuilderRule perform(Consumer<PlanNodeBuilder> action) {
            return new PlanNodeBuilderRule(this, action);
        }


        static PlanNodeBuilderPredicate withAny(
            Function<PlanNodeBuilder,Stream<PlanNodeBuilder>> stream,
            Predicate<PlanNodeBuilder> predicate)
        {
            return node->stream.apply(node).anyMatch(predicate);
        }

        static PlanNodeBuilderPredicate withAll(
            Function<PlanNodeBuilder,Stream<PlanNodeBuilder>> stream,
            Predicate<PlanNodeBuilder> predicate)
        {
            return node->stream.apply(node).allMatch(predicate);
        }


        static PlanNodeBuilderPredicate withSome(
            LongPredicate number,
            Function<PlanNodeBuilder,Stream<PlanNodeBuilder>> stream,
            Predicate<PlanNodeBuilder> predicate)
        {
            return node->number.test(stream.apply(node).filter(predicate).count());
        }





        static PlanNodeBuilderPredicate withProperty(String key, String value) {
            return node->value.equals(node.properties().get(key));
        }

        static PlanNodeBuilderPredicate withTag(String tag) {
            return node->node.tags().contains(tag);
        }

        static PlanNodeBuilderPredicate withParent(Predicate<PlanNodeBuilder> predicate) {
            return node->node.parent().filter(predicate).isPresent();
        }



        static PlanNodeBuilderPredicate withAnyChild(Predicate<PlanNodeBuilder> predicate) {
            return withAny(PlanNodeBuilder::children,predicate);
        }

        static PlanNodeBuilderPredicate withEveryChild(Predicate<PlanNodeBuilder> predicate) {
            return withAll(PlanNodeBuilder::children,predicate);
        }

        static PlanNodeBuilderPredicate withSomeChildren(
            LongPredicate number,
            Predicate<PlanNodeBuilder> predicate
        ) {
            return withSome(number,PlanNodeBuilder::children,predicate);
        }

        static PlanNodeBuilderPredicate withAnyDescendant(Predicate<PlanNodeBuilder> predicate) {
            return withAny(PlanNodeBuilder::descendants,predicate);
        }

        static PlanNodeBuilderPredicate withEveryDescendant(Predicate<PlanNodeBuilder> predicate) {
            return withAll(PlanNodeBuilder::descendants,predicate);
        }

        static PlanNodeBuilderPredicate withSomeDescendants(
            LongPredicate number,
            Predicate<PlanNodeBuilder> predicate
        ) {
            return withSome(number,PlanNodeBuilder::children,predicate);
        }

        static PlanNodeBuilderPredicate withAnyAncestor(Predicate<PlanNodeBuilder> predicate) {
            return withAny(PlanNodeBuilder::ancestors,predicate);
        }

        static PlanNodeBuilderPredicate withEveryAncestor(Predicate<PlanNodeBuilder> predicate) {
            return withAll(PlanNodeBuilder::ancestors,predicate);
        }

        static PlanNodeBuilderPredicate withSomeAncestors(
            LongPredicate number,
            Predicate<PlanNodeBuilder> predicate
        ) {
            return withSome(number,PlanNodeBuilder::ancestors,predicate);
        }

        static PlanNodeBuilderPredicate withAnySibling(Predicate<PlanNodeBuilder> predicate) {
            return withAny(PlanNodeBuilder::siblings,predicate);
        }

        static PlanNodeBuilderPredicate withEverySibling(Predicate<PlanNodeBuilder> predicate) {
            return withAll(PlanNodeBuilder::siblings,predicate);
        }

        static PlanNodeBuilderPredicate withSomeSiblings(
            LongPredicate number,
            Predicate<PlanNodeBuilder> predicate
        ) {
            return withSome(number,PlanNodeBuilder::siblings,predicate);
        }
    }




        public static class PlanNodeBuilderRule {

            private final PlanNodeBuilderPredicate predicate;
            private final Consumer<PlanNodeBuilder> action;


            public PlanNodeBuilderRule (
                PlanNodeBuilderPredicate predicate,
                Consumer<PlanNodeBuilder> action
               ) {
                this.predicate = predicate;
                this.action = action;
            }

            public Consumer<PlanNodeBuilder> action() {
                return action;
            }

            public PlanNodeBuilderPredicate predicate() {
                return predicate;
            }
        }




}
