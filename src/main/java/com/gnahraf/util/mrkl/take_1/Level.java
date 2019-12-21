/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util.mrkl.take_1;

 import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Supports breadth-first traversal of a Merkle tree.
 */
public class Level {
  

  private final Level parent;
  
  /**
   * Integral value of level.
   */
  private final int level;
  private final Node promoRight;
  private final List<Node> nodes;
  

  /**
   * 
   */
  public Level(Tree tree) {
    Objects.requireNonNull(tree);
    this.parent = null;
    this.level = tree.getRoot().height();
    this.promoRight = null;
    this.nodes = Collections.singletonList(tree.getRoot());
  }
  
  
  public Level(Level parent) {
    this.parent = Objects.requireNonNull(parent);
    if (parent.isLeaves())
      throw new IllegalArgumentException("arg is leaves: " + parent);
    this.level = parent.level - 1;
    
    ArrayList<Node> layer = new ArrayList<>(parent.nodes.size() * 2 + 1);
    int lastIndex = parent.nodes.size() - 1;
    for (int i = 0; i < lastIndex; ++i) {
      Node node = parent.nodes.get(i);
      assert level == node.getLeft().height();
      assert level == node.getRight().height();
      layer.add(node.getLeft());
      layer.add(node.getRight());
    }
    Node last = parent.nodes.get(lastIndex);
    assert level == last.getLeft().height();
    layer.add(last.getLeft());
    Node right = last.getRight();
    if (right.height() == level) {
      layer.add(right);
      promoRight = null;
    } else {
      promoRight = right;
      if (promoRight.height() > level)
        throw new AssertionError(promoRight.height() + " > " + level);
    }
    
    ArrayList<Node> levelCarry = new ArrayList<>(1);
    while (!parent.isRoot()) {
      if (parent.carryHeight() == level)
        levelCarry.add(parent.promoRight);
      parent = parent.getParent();
    }
    if (!levelCarry.isEmpty()) {
      if (levelCarry.size() != 1)
        throw new AssertionError(
            "multiple carries at height " + level + ": " + levelCarry, null);
      
      layer.add(levelCarry.get(0));
    }
    this.nodes = Collections.unmodifiableList(layer);
  }
  
  
  
  /**
   * Returns the bottom-most level (the leaves) for the given <tt>tree</tt>.
   * From there you can navigate to any level via {@linkplain #getParent()}.
   * 
   * @see #heap()
   */
  public static Level getLeaves(Tree tree) {
    Level level = new Level(tree);
    while (!level.isLeaves())
      level = new Level(level);
    
    return level;
  }
  
  
  
  /**
   * Determines if there's no level above this.
   */
  public boolean isRoot() {
    return parent == null;
  }
  
  /**
   * Determines if there's no level below this.
   */
  public boolean isLeaves() {
    return level == 0;
  }
  
  /**
   * Returns the parent level if this instance is not root; <tt>null</tt>, o.w.
   */
  public Level getParent() {
    return parent;
  }
  
  
  public Level getParent(int height) throws IndexOutOfBoundsException {
    if (height < this.level)
      throw new IndexOutOfBoundsException(
          "argument " + height + " < instance height " + this.level);
    
    Level level = this;
    for (; level.level < height; level = level.parent)
      if (level.isRoot())
        throw new IndexOutOfBoundsException(
            "argument " + height + " > root height " + level.level);
    
    return level;
  }
  
  
  /**
   * Returns a mutable list of levels starting from <tt>this</tt> and its successive
   * parents. The last level in the returned list is root.
   */
  public List<Level> heap() {
    ArrayList<Level> heap = new ArrayList<>();
    for (Level level = this; level != null; level = level.parent)
      heap.add(level);
    return heap;
  }
  
  
  /**
   * Returns the parent node of the node <tt>index</tt>ed in {@linkplain #nodes()}.
   * Note this takes care of so called "promoted" nodes--"carry", in our terminology here
   * (which if present, lands as the <em>last</em> node in this level).
   * If invoked with the maximum <tt>index</tt>, the retuned parent node's height then
   *  <em>may</em> be greater than 1 plus this level's.
   * 
   * @return the parent node, if not the root instance; <tt>null</tt>, o.w.
   */
  public Node getParentNode(int index) throws IndexOutOfBoundsException {
    int maxIndex = nodes.size() - 1;
    if (index < 0 || index > maxIndex)
      throw new IndexOutOfBoundsException(index);
      
    if (isRoot())
      return null;
    
    Node parentNode;
    
    int pindex = index / 2;
    if (pindex == parent.nodes.size()) {
      Level parentLevel = null;
      
      for (Level pLevel = parent; !pLevel.isRoot(); pLevel = pLevel.parent) {
        if (pLevel.carryHeight() == level) {
          parentLevel = pLevel.parent;
          break;
        }
      }
      // (note our confidence parentLevel != null)
      pindex = parentLevel.nodes.size() - 1;
      parentNode = parentLevel.nodes.get(pindex);
    
    } else
      parentNode = parent.nodes.get(pindex);
    
    return parentNode;
  }
  
  
  public List<Node> pathToRoot(int index) {
    
    ArrayList<Node> path = new ArrayList<>();
    path.add(nodes.get(index));
    
    for (Level level = this; !level.isRoot() ; ) {
      Node parentNode = level.getParentNode(index);
      path.add(parentNode);
      
      int pHeight = parentNode.height();
      
      if (pHeight == level.level + 1) {
        level = level.parent;
        index = index / 2;
      
      } else {
        level = level.getParent(pHeight);
        index = level.nodes().size() - 1;
        assert index >= 0;
      }
    }
    return path;
  }
  
  
  /**
   * Returns an immutable list of nodes at this level, ordered left to right.
   */
  public List<Node> nodes() {
    return nodes;
  }
  
  /**
   * Returns the height of this level. Under this convention, the deepest
   * level (leaves) has height zero; the root is at the height level.
   * @return
   */
  public int level() {
    return level;
  }
  
  
  /**
   * Determines if the sibling of the last node at this level is a promoted
   * node. (Discovered on the way down.)
   */
  public boolean hasPromo() {
    return promoRight != null;
  }
  
  /**
   * Returns the <em>promoted</em> node, if any, from the parent level.
   * A promoted node is one that was stitched as the right child of the last node in the
   * parent level. The following invariants hold for non-null return values:
   * <ol>
   * <li>The {@linkplain Node#height() height} of the promoted node is less than that of this level.</li>
   * <li>For every <tt>Level</tt> there is at most <em>one</em> promoted node (in the tree, but more
   * specifically, from its parent levels) that contribute to the list of {@linkplain #nodes()
   * nodes} at that level.</li>
   * </ol> 
   */
  public Node promo() {
    return promoRight;
  }
  
  /**
   * Returns the promoted node's height, if any; -1 o.w.
   */
  public int carryHeight() {
    return promoRight == null ? -1 : promoRight.height();
  }
  

}
