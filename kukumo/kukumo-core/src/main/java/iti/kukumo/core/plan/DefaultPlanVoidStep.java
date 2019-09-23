package iti.kukumo.core.plan;

import iti.kukumo.api.plan.PlanNode;

public class DefaultPlanVoidStep extends DefaultPlanStep {


    public DefaultPlanVoidStep(PlanNode node) {
        super();
        this
        .setName(node.name())
        .setKeyword(node.keyword())
        .setLanguage(node.language())
        .setSource(node.source());
    }


    @Override
    public boolean isVoid() {
        return true;
    }

    @Override
    public DefaultPlanStep setId(String id) {
        return super.setId(id);
    }
}
