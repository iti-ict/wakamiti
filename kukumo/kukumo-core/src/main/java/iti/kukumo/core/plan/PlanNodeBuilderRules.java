package iti.kukumo.core.plan;

import iti.kukumo.api.KukumoException;

import java.util.Objects;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Stream;


public class PlanNodeBuilderRules {


    private enum RuleMethod { ALL, ANY, FIRST }

    public interface PlanNodeBuilderRule {
        void apply(PlanNodeBuilder node) throws KukumoException;
    }


    public static class PlanNodeBuilderRuleConsumer implements  PlanNodeBuilderRule {

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

        public PlanNodeBuilderRuleConsumer and() {
            return this;
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

        public void apply(PlanNodeBuilder plan) throws KukumoException {
            switch (method) {
                case ALL:
                    plan.descendants().filter(predicate).forEach(consumer);
                    break;
                case ANY:
                    plan.descendants().filter(predicate).findAny().ifPresent(consumer);
                    break;
                case FIRST:
                    plan.descendants().filter(predicate).findFirst().ifPresent(consumer);
                    break;
            }
        }

        public PlanNodeBuilderRuleBiConsumer given(Function<PlanNodeBuilder,Optional<PlanNodeBuilder>> rightNodeGetter) {
            return new PlanNodeBuilderRuleBiConsumer(predicate,consumer,method, rightNodeGetter);
        }
    }





    public static class PlanNodeBuilderRuleBiConsumer extends PlanNodeBuilderRuleConsumer {

        protected final Function<PlanNodeBuilder,Optional<PlanNodeBuilder>> rightNodeGetter;

        private PlanNodeBuilderRuleBiConsumer(
                Predicate<PlanNodeBuilder> predicate,
                Consumer<PlanNodeBuilder> consumer,
                RuleMethod method,
                Function<PlanNodeBuilder,Optional<PlanNodeBuilder>> rightNodeGetter
        ) {
            super(predicate,consumer,method);
            this.rightNodeGetter = rightNodeGetter;
        }


        public PlanNodeBuilderRuleBiConsumer and() {
            return this;
        }

        public PlanNodeBuilderRuleBiConsumer perform(BiConsumer<PlanNodeBuilder,PlanNodeBuilder> action) {
            return new PlanNodeBuilderRuleBiConsumer(predicate, biconsume(rightNodeGetter,action), method, rightNodeGetter);
        }

    }




    private static Consumer<PlanNodeBuilder> biconsume(
             Function<PlanNodeBuilder,Optional<PlanNodeBuilder>> rightNodeGetter,
             BiConsumer<PlanNodeBuilder, PlanNodeBuilder> action
     ) {
         return leftNode -> rightNodeGetter.apply(leftNode).ifPresent( rightNode -> action.accept(leftNode, rightNode));
     }



    public static PlanNodeBuilderRuleConsumer forEachNode(Predicate<PlanNodeBuilder> predicate) {
        return new PlanNodeBuilderRuleConsumer(predicate,x->{},RuleMethod.ALL);
    }


    public static PlanNodeBuilderRuleConsumer forFirstNode(Predicate<PlanNodeBuilder> predicate) {
        return new PlanNodeBuilderRuleConsumer(predicate,x->{},RuleMethod.FIRST);
    }


    public static PlanNodeBuilderRuleConsumer forOneNode(Predicate<PlanNodeBuilder> predicate) {
        return new PlanNodeBuilderRuleConsumer(predicate,x->{},RuleMethod.ANY);
    }


    public static Function<PlanNodeBuilder,Optional<PlanNodeBuilder>> anyOtherNode(Predicate<PlanNodeBuilder> predicate, BiPredicate<PlanNodeBuilder,PlanNodeBuilder> biPredicate) {
        return leftNode -> leftNode.root().descendants().filter(predicate).filter(rightNode -> biPredicate.test(leftNode,rightNode)).findAny();
    }


    public static <T> BiPredicate<PlanNodeBuilder,PlanNodeBuilder> withSame (Function<PlanNodeBuilder,T> method) {
        return (leftNode,rightNode) -> Objects.equals(method.apply(leftNode),method.apply(rightNode));
    }



    public static Predicate<PlanNodeBuilder> withAny(
            Function<PlanNodeBuilder,Stream<PlanNodeBuilder>> stream,
            Predicate<PlanNodeBuilder> predicate)
    {
        return node->stream.apply(node).anyMatch(predicate);
    }

     public static Predicate<PlanNodeBuilder> withAll(
            Function<PlanNodeBuilder,Stream<PlanNodeBuilder>> stream,
            Predicate<PlanNodeBuilder> predicate)
     {
        return node->stream.apply(node).allMatch(predicate);
     }


     public static Predicate<PlanNodeBuilder> withSome(
            LongPredicate number,
            Function<PlanNodeBuilder,Stream<PlanNodeBuilder>> stream,
            Predicate<PlanNodeBuilder> predicate)
     {
        return node->number.test(stream.apply(node).filter(predicate).count());
     }



     public static Predicate<PlanNodeBuilder> withProperty(String key, String value) {
        return node->value.equals(node.properties().get(key));
     }


     public static Predicate<PlanNodeBuilder> withTag(String tag) {
        return node->node.tags().contains(tag);
     }


     public static Predicate<PlanNodeBuilder> withParent(Predicate<PlanNodeBuilder> predicate) {
        return node->node.parent().filter(predicate).isPresent();
     }



     public static Predicate<PlanNodeBuilder> withAnyChild(Predicate<PlanNodeBuilder> predicate) {
        return withAny(PlanNodeBuilder::children,predicate);
    }

     public static Predicate<PlanNodeBuilder> withEveryChild(Predicate<PlanNodeBuilder> predicate) {
        return withAll(PlanNodeBuilder::children,predicate);
    }

     public static Predicate<PlanNodeBuilder> withSomeChildren(
            LongPredicate number,
            Predicate<PlanNodeBuilder> predicate
    ) {
        return withSome(number,PlanNodeBuilder::children,predicate);
    }

     public static Predicate<PlanNodeBuilder> withAnyDescendant(Predicate<PlanNodeBuilder> predicate) {
        return withAny(PlanNodeBuilder::descendants,predicate);
    }

     public static Predicate<PlanNodeBuilder> withEveryDescendant(Predicate<PlanNodeBuilder> predicate) {
        return withAll(PlanNodeBuilder::descendants,predicate);
    }

     public static Predicate<PlanNodeBuilder> withSomeDescendants(
            LongPredicate number,
            Predicate<PlanNodeBuilder> predicate
    ) {
        return withSome(number,PlanNodeBuilder::children,predicate);
    }

     public static Predicate<PlanNodeBuilder> withAnyAncestor(Predicate<PlanNodeBuilder> predicate) {
        return withAny(PlanNodeBuilder::ancestors,predicate);
    }

     public static Predicate<PlanNodeBuilder> withEveryAncestor(Predicate<PlanNodeBuilder> predicate) {
        return withAll(PlanNodeBuilder::ancestors,predicate);
    }

     public static Predicate<PlanNodeBuilder> withSomeAncestors(
            LongPredicate number,
            Predicate<PlanNodeBuilder> predicate
    ) {
        return withSome(number,PlanNodeBuilder::ancestors,predicate);
    }

     public static Predicate<PlanNodeBuilder> withAnySibling(Predicate<PlanNodeBuilder> predicate) {
        return withAny(PlanNodeBuilder::siblings,predicate);
    }

     public static Predicate<PlanNodeBuilder> withEverySibling(Predicate<PlanNodeBuilder> predicate) {
        return withAll(PlanNodeBuilder::siblings,predicate);
    }

     public static Predicate<PlanNodeBuilder> withSomeSiblings(
            LongPredicate number,
            Predicate<PlanNodeBuilder> predicate
    ) {
        return withSome(number,PlanNodeBuilder::siblings,predicate);
    }


     public static Predicate<PlanNodeBuilder> anyNode(Predicate<PlanNodeBuilder> predicate) {
        return predicate;
     }


    public static Predicate<PlanNodeBuilder> childOf(Predicate<PlanNodeBuilder> predicate) {
        return node -> node.parent().map(predicate::test).orElse(false);
    }

}
