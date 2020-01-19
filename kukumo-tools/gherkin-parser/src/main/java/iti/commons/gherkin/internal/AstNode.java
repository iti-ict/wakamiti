package iti.commons.gherkin.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import iti.commons.gherkin.Comment;
import iti.commons.gherkin.internal.Parser.RuleType;
import iti.commons.gherkin.internal.Parser.TokenType;

public class AstNode {

    private final Map<RuleType, List<Object>> subItems = new EnumMap<>(RuleType.class);
    public final RuleType ruleType;

    public AstNode(RuleType ruleType) {
        this.ruleType = ruleType;
    }

    public void add(RuleType ruleType, Object obj) {
        List<Object> items = subItems.get(ruleType);
        if (items == null) {
            items = new ArrayList<>();
            subItems.put(ruleType, items);
        }
        items.add(obj);
    }


    public void add(Parser.RuleType ruleType, Object obj, List<Comment> comments) {
        add(ruleType, obj);
    }



    @SuppressWarnings("unchecked")
    public <T> T getSingle(RuleType ruleType, T defaultResult) {
        List<Object> items = getItems(ruleType);
        return (T) (items.isEmpty() ? defaultResult : items.get(0));
    }


    @SuppressWarnings("unchecked")
    public <T> List<T> getItems(RuleType ruleType) {
        List<T> items = (List<T>) subItems.get(ruleType);
        if (items == null) {
            return Collections.emptyList();
        }
        return items;
    }

    public Token getToken(TokenType tokenType) {
        RuleType tokenRuleType = RuleType.cast(tokenType);
        return getSingle(tokenRuleType, new Token(null, null));
    }

    public List<Token> getTokens(TokenType tokenType) {
        return getItems(RuleType.cast(tokenType));
    }
}
