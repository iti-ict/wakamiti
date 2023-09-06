/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

/**
 * @author Luis Iñesta Gelabert - linesta@iti.es | luiinge@gmail.com
 */
package es.iti.wakamiti.core.util;


import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class TokenParser {

    private final List<Pattern> tokens;

    private String remainString;
    private String nextToken;


    public TokenParser(String string, List<String> literals, List<String> regex) {
        this.remainString = string;
        this.tokens = regex.stream().map(TokenParser::regex).collect(Collectors.toList());
        for (String literal : literals) {
            this.tokens.add(TokenParser.literal(literal));
        }
        computeNextToken();
    }


    public boolean hasMoreTokens() {
        return nextToken != null;
    }


    public String nextToken() {
        String token = this.nextToken;
        computeNextToken();
        return token;
    }


    private void computeNextToken() {
        nextToken = computeMaxToken(remainString);
        if (nextToken == null) {
            remainString = null;
        } else {
            remainString = remainString.substring(nextToken.length());
        }
    }


    private String computeMaxToken(String string) {
        return tokens.stream()
            .map(token -> token.matcher(string))
            .filter(Matcher::matches)
            .map(matcher -> matcher.group(1))
            .reduce((match1, match2) -> match1.length() > match2.length() ? match1 : match2)
            .orElse(null);
    }


    private static Pattern regex(String regex) {
        return Pattern.compile("(" + regex + ").*");
    }


    private static Pattern literal(String literal) {
        return Pattern.compile("(" + Pattern.quote(literal) + ").*");
    }

}