/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package es.iti.wakamiti.api.plan;


/**
 * Represents different types of plan nodes.
 *
 * @author Luis Iñesta Gelabert - linesta@iti.es
 */
public enum NodeType {

    /**
     * Regular node that aggregates other nodes.
     */
    AGGREGATOR,

    /**
     * Root node for an individual test case.
     */
    TEST_CASE,

    /**
     * Aggregator node within a test case.
     */
    STEP_AGGREGATOR,

    /**
     * Executable final node within a test case. Cannot
     * have children.
     */
    STEP,

    /**
     * Non-executable final node within a test case.
     * Cannot have children.
     */
    VIRTUAL_STEP;

    /**
     * Checks if the current node type matches any of
     * the specified node types.
     *
     * @param nodeTypes The node types to check against
     * @return {@code true} if the node type matches
     * any of the specified types, {@code false} otherwise
     */
    public boolean isAnyOf(NodeType... nodeTypes) {
        for (NodeType nodeType : nodeTypes) {
            if (nodeType == this) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the current node type does not match any
     * of the specified node types.
     *
     * @param nodeTypes The node types to check against
     * @return {@code true} if the node type does not match
     * any of the specified types, {@code false} otherwise
     */
    public boolean isNoneOf(NodeType... nodeTypes) {
        for (NodeType nodeType : nodeTypes) {
            if (nodeType == this) {
                return false;
            }
        }
        return true;
    }

}