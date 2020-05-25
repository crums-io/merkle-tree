/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.util.mrkl.index;

/**
 * Breadth-first coordinates of a node in a Merkle tree.
 */
public abstract class AbstractNode {

  /**
   * Stateless factory.
   */
  public static TreeIndex.NodeFactory<AbstractNode> FACTORY =
    new TreeIndex.NodeFactory<AbstractNode>() {
      @Override
      public void init(TreeIndex<AbstractNode> host) {   }
      @Override
      public AbstractNode newNode(int level, int index, boolean right) {
        if (index == 0 && right)
          throw new IllegalArgumentException("index 0 must be left");
        
        return new AbstractNode(level, index) {
          @Override
          public boolean isRight() {
            return right;
          }
        };
      }
    };
  
  
  
  private final int level;
  private final int index;

  protected AbstractNode(int level, int index) throws IndexOutOfBoundsException {
    this.level = level;
    this.index = index;
    
    if (level < 0 || level > 32)
      throw new IndexOutOfBoundsException("level: " + level);
    if (index < 0)
      throw new IndexOutOfBoundsException("index: " + index);
  }
  
  
  /**
   * Returns this node's level. Levels are counted from the bottom up: zero at the leaves,
   * maximum at root.
   */
  public final int level() {
    return level;
  }
  
  
  /**
   * Determines whether this node is at level zero.
   */
  public final boolean isLeaf() {
    return level == 0;
  }
  
  /**
   * Returns this node's index (at this level).
   */
  public final int index() {
    return index;
  }
  
  
  /**
   * Determines whether this node is the <em>left</em> child of its parent.
   * Note, for closure reasons the root node is defined to be left.
   */
  public final boolean isLeft() {
    return !isRight();
  }
  
  /**
   * Determines whether this node is the <em>right</em> child of its parent.
   * <p>
   * Implementations should be marked <strong>final</strong>.
   * </p>
   */
  public abstract boolean isRight();
  

  
  
  /**
   * Equality semantics are governed by coordinates and handedness.
   */
  @Override
  public final boolean equals(Object o) {
    if (this == o)
      return true;
    else if (o instanceof AbstractNode) {
      AbstractNode other = (AbstractNode) o;
      return index == other.index && level == other.level && isRight() == other.isRight();
    } else
      return false;
  }
  
  
  @Override
  public final int hashCode() {
    int state = level * 814279 + index;
    if (isRight())
      state = -state;
    return state;
  }
  
  
  @Override
  public String toString() {
    return "(" + level + ":" + index + ")";
  }

}
