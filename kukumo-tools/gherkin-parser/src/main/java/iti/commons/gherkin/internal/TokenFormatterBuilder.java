/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package iti.commons.gherkin.internal;

public class TokenFormatterBuilder implements Parser.Builder<String> {
    private final TokenFormatter formatter = new TokenFormatter();
    private final StringBuilder tokensTextBuilder = new StringBuilder();

    @Override
    public void build(Token token) {
        tokensTextBuilder.append(formatter.formatToken(token)).append("\n");
    }

    @Override
    public void startRule(Parser.RuleType ruleType) {
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