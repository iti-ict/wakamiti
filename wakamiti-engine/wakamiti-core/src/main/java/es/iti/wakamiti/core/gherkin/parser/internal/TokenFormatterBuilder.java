/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package es.iti.wakamiti.core.gherkin.parser.internal;

import es.iti.wakamiti.core.gherkin.parser.internal.Parser;
import es.iti.wakamiti.core.gherkin.parser.internal.Token;
import es.iti.wakamiti.core.gherkin.parser.internal.TokenFormatter;

public class TokenFormatterBuilder implements es.iti.wakamiti.core.gherkin.parser.internal.Parser.Builder<String> {
    private final es.iti.wakamiti.core.gherkin.parser.internal.TokenFormatter formatter = new TokenFormatter();
    private final StringBuilder tokensTextBuilder = new StringBuilder();

    @Override
    public void build(Token token) {
        tokensTextBuilder.append(formatter.formatToken(token)).append("\n");
    }

    @Override
    public void startRule(es.iti.wakamiti.core.gherkin.parser.internal.Parser.RuleType ruleType) {
    }

    @Override
    public void endRule(Parser.RuleType ruleType) {
    }

    @Override
    public String getResult() {
        return tokensTextBuilder.toString();
    }

    @Override
    public void reset() {
    }
}