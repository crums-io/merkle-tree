/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.util.mrkl;


import java.security.MessageDigest;

import io.crums.util.mrkl.index.AbstractNode;

/**
 * Merkle tree node. Besides its coordinates (level, index), each node holds
 * {@linkplain #data() data}, which for internal nodes is a fixed-width signature.
 * (The leaf nodes can be anything--including another signature.) Instances are
 * immutable, so they're safe to pass around.
 * 
 * 
 * <h2>Navigation</h2>
 * 
 * Supports navigating to parent, siblings, and children--as well as random access.
 * Note these access methods return a <em>new</em> object every time they're invoked.
 * (This is to minimize the memory footprint of large trees.) Equality and hashCode
 * semantics are properly implemented, so as long as you don't compare instances by
 * reference, you'll be OK.
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
  
  /**
   * Returns the node's <em>serial</em> index. This is equal to the number of nodes preceding this node
   * in a breadth-first traversal of the tree. The root node, therefore, returns 0; the last leaf returns
   * 2 x {@linkplain #leafCount() leafCount} - 2. 
   */
  public final int serialIndex() {
    return tree.idx().serialIndex(level(), index());
  }
  
  
  /**
   * Returns a copy of the node's data. For internal nodes, this is always the hash
   * of the node itself (computed from the hash of its child nodes); for leaf nodes,
   * depending on the tree type, this can be the hash of an item, or an item itself.
   * 
   * @see Tree#data(int, int)
   */
  public byte[] data() {
    return tree.data(level(), index());
  }
  
  
  /**
   * Verifies the hash of this node against its children, if it has any.
   * The verification is <em>not recursive</em>.
   * <p>
   * Mostly only for testing. Reason why: in a Merkle tree, you construct
   * proofs from the bottom up, from leaf to root.
   * </p>
   */
  public boolean verify(MessageDigest digest) {
    return tree.verify(this, digest);
  }
  
  
  
  // Navigation methods
  
  
  public final boolean isRoot() {
    return level() == tree.idx().height();
  }
  
  /**
   * Returns the sibling that makes this node's parent, or <code>null</code>
   * if this node is root.
   */
  public Node sibling() {
    return isRoot() ? null : tree.idx().getSibling(this);
  }
  
  /**
   * Returns this node's parent, or <code>null</code> if this node is root.
   */
  public Node parent() {
    return isRoot() ? null : tree.idx().getParent(this);
  }
  
  /**
   * Returns this node's left child, or <code>null</code> if this node is a leaf.
   * 
   * @see #isLeaf()
   */
  public Node leftChild() {
    return isLeaf() ? null : tree.idx().getLeftChild(this);
  }
  
  /**
   * Returns this node's right child, or <code>null</code> if this node is a leaf.
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
   * Returns the leaf node at the given <code>index</code>.
   * 
   * @param index     zero-based index into leaf count
   * @see #leafCount()
   */
  public Node getLeaf(int index) throws IndexOutOfBoundsException {
    return index() == index && level() == 0 ? this : tree.idx().getNode(0, index);
  }
  
  
  /**
   * Determines if the node is a <em>carry</em>. A node is a carry if
   * it is an internal node and its right child is from a lower level than its
   * left child.
   */
  public final boolean isCarry() {
    return !isLeaf() && leftChild().level() != rightChild().level();
  }
  
  
  /**
   * Returns the total number of leaves in the tree this node belongs to.
   */
  public final int leafCount() {
    return tree.idx().count();
  }
  
  
  /**
   * Returns the tree this node belongs to.
   */
  public Tree tree() {
    return tree;
  }

}
