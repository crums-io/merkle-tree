/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util.mrkl.take_1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 
 */
public class Tree {
  
  private final Node root;
  private final String algo;
  private final List<Node> leaves;

  /**
   * 
   */
  Tree(Node root, String algo, List<Node> leaves) {
    this.root = Objects.requireNonNull(root, "root");
    this.algo = Objects.requireNonNull(algo, "algo");
    Objects.requireNonNull(leaves, "leaves");
    {
      ArrayList<Node> list = new ArrayList<>(leaves.size());
      list.addAll(leaves);
      this.leaves = Collections.unmodifiableList(list);
    }
    
    if (leaves.size() < 2)
      throw new IllegalArgumentException("count: " + leaves.size());
    if (root.isLeaf())
      throw new IllegalArgumentException("root node is a leaf: " + root);
  }

  /**
   * Returns the root node.
   */
  public final Node getRoot() {
    return root;
  }

  /**
   * Returns the name of the algorithm with which the tree's internal nodes were constructed.
   */
  public final String getAlgo() {
    return algo;
  }

  /**
   * Returns the number of leaf nodes in the tree.
   */
  public final int getCount() {
    return leaves.size();
  }
  
  
  public final List<Node> getLeaves() {
    return leaves;
  }

}
