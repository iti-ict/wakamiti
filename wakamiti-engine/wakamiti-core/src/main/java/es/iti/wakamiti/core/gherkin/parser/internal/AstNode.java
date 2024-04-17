/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.gherkin.parser.internal;


import java.util.*;

import es.iti.wakamiti.core.gherkin.parser.Comment;
import es.iti.wakamiti.core.gherkin.parser.internal.Parser.RuleType;
import es.iti.wakamiti.core.gherkin.parser.internal.Parser.TokenType;


public class AstNode {

    private final Map<RuleType, List<Object>> subItems = new EnumMap<>(RuleType.class);
    public final RuleType ruleType;

    public AstNode(RuleType ruleType) {
        this.ruleType = ruleType;
    }

    public void add(RuleType ruleType, Object obj) {
       subItems.computeIfAbsent(ruleType, k -> new ArrayList<>()).add(obj);
//        List<Object> items = subItems.get(ruleType);
//        if (items == null) {
//            items = new ArrayList<>();
//            subItems.put(ruleType, items);
//        }
//        items.add(obj);
    }


    public void add(RuleType ruleType, Object obj, List<Comment> comments) {
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