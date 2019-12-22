/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util.mrkl;

import com.gnahraf.util.mrkl.index.AbstractNode;
import com.gnahraf.util.mrkl.index.TreeIndex;

/**
 * 
 */
public class Node extends AbstractNode {

  
  private final Tree tree;
  
  Node(int level, int index, Tree tree) {
    super(level, index);
    this.tree = tree;
  }

  @Override
  public final boolean isRight() {
    return tree.idx().isRight(level(), index());
  }
  
  
  public final int serialIndex() {
    return tree.idx().serialIndex(level(), index());
  }
  
  
  public byte[] data() {
    return tree.data(level(), index());
  }
  
  
  
  
  
  // Navigation methods
  
  
  public final boolean isRoot() {
    return level() == tree.idx().height();
  }
  
  /**
   * Returns the sibling that makes this node's parent, or <tt>null</tt>
   * if this node is root.
   */
  public Node sibling() {
    return isRoot() ? null : tree.idx().getSibling(this);
  }
  
  /**
   * Returns this node's parent, or <tt>null</tt> if this node is root.
   */
  public Node parent() {
    return isRoot() ? null : tree.idx().getParent(this);
  }
  
  /**
   * Returns this node's left child, or <tt>null</tt> if this node is a leaf.
   * 
   * @see #isLeaf()
   */
  public Node leftChild() {
    return isLeaf() ? null : tree.idx().getLeftChild(this);
  }
  
  /**
   * Returns this node's right child, or <tt>null</tt> if this node is a leaf.
   * 
   * @see #isLeaf()
   */
  public Node rightChild() {
    return isLeaf() ? null : tree.idx().getRightChild(this);
  }
  
  
  /**
   * Returns the root of the tree.
   */
  public Node getRoot() {
    return isRoot() ? this : tree.idx().getNode(tree.idx().height(), 0);
  }
  
  /**
   * Returns the leaf node at the given <tt>index</tt>.
   * 
   * @param index     zero-based index into leaf count
   * @see #leafCount()
   */
  public Node getLeaf(int index) throws IndexOutOfBoundsException {
    return index() == index && level() == 0 ? this : tree.idx().getNode(0, index);
  }
  
  
  public final boolean isCarry() {
    return !isLeaf() && leftChild().level() != rightChild().level();
  }
  
  
  
  public final int leafCount() {
    return tree.idx().count();
  }
  
  public TreeIndex<Node> getTree() {
    return tree.idx();
  }

}
