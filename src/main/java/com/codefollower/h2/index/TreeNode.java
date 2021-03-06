/*
 * Copyright 2004-2011 H2 Group. Multiple-Licensed under the H2 License,
 * Version 1.0, and under the Eclipse Public License, Version 1.0
 * (http://h2database.com/html/license.html).
 * Initial Developer: H2 Group
 */
package com.codefollower.h2.index;

import com.codefollower.h2.result.Row;

/**
 * Represents a index node of a tree index.
 */
class TreeNode {

    /**
     * The balance. For more information, see the AVL tree documentation.
     */
    int balance;

    /**
     * The left child node or null.
     */
    TreeNode left;

    /**
     * The right child node or null.
     */
    TreeNode right;

    /**
     * The parent node or null if this is the root node.
     */
    TreeNode parent;

    /**
     * The row.
     */
    Row row;

    TreeNode(Row row) {
        this.row = row;
    }

    /**
     * Check if this node is the left child of its parent. This method returns
     * true if this is the root node.
     *
     * @return true if this node is the root or a left child
     */
    boolean isFromLeft() {
        return parent == null || parent.left == this;
    }

}
