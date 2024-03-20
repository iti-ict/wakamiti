/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.core.util;


import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Pattern;


/**
 * Implementation of the Ratcliff/Obershelp Pattern Matching Algorithm as
 * described in the July 1988 issue of <a href=http://www.ddj.com/184407970?pgno=5>Dr. Dobbs Journal</a>
 *
 * @author Marco Petris
 * @author Luis Iñesta - luiinge@gmail.com (Additional tweaks and fixes)
 */
public class Simil {

    private final String upBaseInput;


    /**
     * Constructor.
     *
     * @param baseInput the basic input to compare against
     */
    public Simil(String baseInput) {
        upBaseInput = baseInput.toUpperCase();
    }


    /**
     * This method takes two strings, one from each stack, and looks for the
     * largest substring which both have in common. The fragments of the two
     * strings which do not belong to the common substring are pushed on the
     * stacks. The size of the common substring is returned.
     *
     * @param baseInputStack the stack with the remaining portions of the base
     *                       input string which await examination
     * @param inputStack     the stack with the remaining portions of the input
     *                       string which await examination
     * @return the size of the larges common substring of the two strings which
     * were on the tops of the incoming stacks.
     */
    private int compare(Deque<String> baseInputStack, Deque<String> inputStack) {

        String comp1 = baseInputStack.pop();
        String comp2 = inputStack.pop();

        // we start with the largest possible size
        int windowSize = Math.min(comp1.length(), comp2.length());

        // loop over the decrementing size until we find
        // a common substring
        while (windowSize > 0) {

            // we start to compare substrings of the current window size
            // from the beginning of the base input fragment
            // and move forward with this window by one-character-steps until
            // we find a match
            int pos = 0;
            while (pos + windowSize - 1 < comp1.length()) {

                // is this a common substring?
                if (comp2.contains(comp1.substring(pos, pos + windowSize))) {

                    compareLength(baseInputStack, inputStack, comp1, comp2, windowSize, pos);
                    // this is the size of the first largest substring we could find
                    return windowSize;
                }
                // nothing so far, so we move the window one character forward
                pos++;
            }
            // nothing so far, so we make the window (substring size) smaller
            windowSize--;
        }

        return 0;
    }

    private void compareLength(Deque<String> baseInputStack, Deque<String> inputStack, String comp1, String comp2, int windowSize, int pos) {
        // yes, so we take the parts that do not belong to our matching
        // string and push them onto the stack for later examination
        String[] comp2Rest = comp2.split(Pattern.quote(comp1.substring(pos, pos + windowSize)), 2);
        String[] comp1Rest = comp1.split(Pattern.quote(comp1.substring(pos, pos + windowSize)), 2);

        // both rest-arrays should have at least one entry to compare to next time
        int resultLen = Math.min(comp1Rest.length, comp2Rest.length);
        if (resultLen > 1) {
            // we do not push empty fragments onto the stack
            // but everything else
            for (int idx = 0; idx < resultLen; idx++) {
                if (!"".equals(comp1Rest[idx])) {
                    baseInputStack.push(comp1Rest[idx]);
                }
                if (!"".equals(comp2Rest[idx])) {
                    inputStack.push(comp2Rest[idx]);
                }
            }
        }
    }


    /**
     * Computes the similarity of the base input of this instance to the given
     * input and returns the computed value in percent.
     *
     * @param input the input to compare with the basic input of this instance
     * @return the percentage value of similarity
     */
    public double getSimilarityInPercentFor(String input) {
        String upInput = input.toUpperCase();
        Deque<String> inputStack = new ArrayDeque<>();
        Deque<String> baseInputStack = new ArrayDeque<>();

        baseInputStack.push(upBaseInput);
        inputStack.push(upInput);

        // total length of the matching substrings
        int compCount = 0;

        // we loop over the portions of the strings and try to find
        // the lengths of largest substrings
        // the stacks get filled when common substrings are found
        while (!inputStack.isEmpty() && !baseInputStack.isEmpty()) {

            compCount += compare(baseInputStack, inputStack);

        }

        // compute the percent value for the total length of the matching substrings
        // regarding the combined total length of the two strings we compared
        double percentVal = (compCount * 2);
        percentVal /= (upBaseInput.length() + upInput.length());

        return (int) (Math.round(percentVal * 100.0));
    }
}