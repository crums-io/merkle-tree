/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util.mrkl.index;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A breadth-first view of the <em>structure</em> of a Merkle tree. In this view
 * each node is represented by 2 coordinates <tt>(<em>level, index</em>)</tt>.
 * Levels are counted up from the leaf nodes, with the leaves at level zero; <em>
 * index</em> is just an index into nodes at that level. Instances are immutable and
 * safe under concurrent access.
 * 
 * <h3>Terminology</h3>
 * <p>
 * Most expositions use the term "promoted" to describe how the last <em>odd</em> node at a
 * level is stitched up the higher node levels to root. (Note in our zero-based index, the
 * index of this last odd node is in fact even; the <em>count</em> is odd.) Here, we use the term <em>
 * carry</em> to represent this special type of joining of two lower level nodes to form a
 * higher level parent. Except for the edge case involving such carries, a node's children
 * are always at adjacent indices at the level just below; for carries, however, a node's
 * children may be from different levels (with the left child always at the same or higher level
 * than the right child).
 * </p><p>
 * <ul>
 * <li><b>Carry.</b> The parent node formed from 2 child nodes at different levels,
 * or a parent node formed if one or more of its descendants have been so formed. 
 * There can only be one such node at any level, and then it may only be the node
 * at the last index at that level.</li>
 * 
 * <li><b>Joins Carry.</b> A child node of a <em>carry</em>. The child node itself may or may not
 * be a carry.
 * </ul>
 * </p><p>
 * Note, the root node is itself usually a carry. The only times it's not is when the number of
 * leaves is an exact power of 2 in which case there are no carries in the tree. Conversely, if
 * a node is a carry, then every ancestor of it (including the root node), every is another carry.</p>
 * 
 * @see AbstractNode
 */
public class TreeIndex<N extends AbstractNode> {
  
  
  public interface NodeFactory<N extends AbstractNode> {
    
    default void init(TreeIndex<N> host) { }
    N newNode(int level, int index, boolean right);
  }
  
  
  
  public static TreeIndex<?> newGeneric(int count) {
    return new TreeIndex<>(count, AbstractNode.FACTORY);
  }
  
  
  
  private final int[] levelCounts;
  private final NodeFactory<N> factory;
  
  

  /**
   * 
   */
  public TreeIndex(int count, NodeFactory<N> factory) {
//    this.count = count;
    this.levelCounts = computeLevelCounts(count);
    this.factory = Objects.requireNonNull(factory, "factory");
    factory.init(this);
  }
  
  /**
   * Returns the number leaf nodes (data items) in the tree.
   */
  public final int count() {
    return levelCounts[0];
  }
  
  /**
   * Returns the total number of nodes in the tree.
   * 
   * @return <tt>2 * count() - 1</tt>
   */
  public final int totalCount() {
    return 2 * count() - 1;
  }
  
  
  /**
   * Returns the total number of carries (otherwise known as promoted nodes).
   */
  public final int totalCarries() {
    return totalCount() - totalCountSansCarries();
  }
  
  
  /**
   * Returns the total number of nodes in the tree excluding the carries.
   * 
   * @return the difference of {@linkplain #totalCount()} and {@linkplain #totalCarries()}
   */
  public final int totalCountSansCarries() {
    int total = 0;
    for (int level = height(); level >= 0; --level)
      total += countSansCarry(level);
    
    return total;
  }
  
  
  
  
  
  public final int serialIndex(int level, int index) throws IndexOutOfBoundsException {
    Objects.checkIndex(index, count(level));
    int zeroIndex = 0;
    for (int h = height(); h > level; --h)
      zeroIndex += count(h);
    return zeroIndex + index;
  }
  
  
  /**
   * Returns the height of the root of the tree relative to its base (the leaves).
   * In this terminology, the leaves are at level zero, and the root is at the level
   * with maximum height.
   * 
   * @see #rootHeightForCount(int)
   */
  public final int height() {
    return levelCounts.length - 1;
  }
  
  
  /**
   * Returns the number of nodes at the given <tt>level</tt>.
   */
  public final int count(int level) throws IndexOutOfBoundsException {
    return levelCounts[level];
  }
  
  /**
   * Returns the numbern of nodes at the given <tt>level</tt> excluding the
   * carry (if it has one).
   */
  public final int countSansCarry(int level) {
    return count() >> level;
  }
  
  
  /**
   * Returns the maximum allowed index at the given <tt>level</tt>.
   * 
   * @return {@linkplain #count(int) count(level)} - 1
   */
  public final int maxIndex(int level) throws IndexOutOfBoundsException {
    return levelCounts[level] - 1;
  }
  
  
  public final boolean maxIndexJoinsCarry(int level) throws IndexOutOfBoundsException {
    return (levelCounts[level] & 1) == 1;
  }
  
  
  /**
   * Determines whether the last node is a carry. There is at most one carry in
   * each level.
   */
  public final boolean hasCarry(int level) throws IndexOutOfBoundsException {
    return count(level) > countSansCarry(level);
  }
  
  
  
  public final N getNode(int level, int index) throws IndexOutOfBoundsException {
    Objects.checkIndex(index, count(level));
    return newNode(level, index, isRight(level, index));
  }
  
  
  
  
  public final N getNode(int serialIndex) throws IndexOutOfBoundsException {
    if (serialIndex < 0)
      throw new IndexOutOfBoundsException(serialIndex);
    int level = height();
    int index = serialIndex;
    for (; level >= 0 ; --level) {
      int count = count(level);
      if (index >= count)
        index -= count;
      else
        break;
    }
    if (level == -1)
      throw new IndexOutOfBoundsException(serialIndex);
    
    return newNode(level, index, isRight(level, index));
  }
  

  /**
   * Convenience method.
   * 
   * @see #getParent(int, int)
   */
  public final N getParent(AbstractNode node) throws IndexOutOfBoundsException {
    return getParent(node.level(), node.index());
  }
  
  public final N getParent(int level, int index) throws IndexOutOfBoundsException {
    AbstractNode sibling = getSibling(level, index);
    if (sibling.isLeft()) {
      level = sibling.level();
      index = sibling.index();
    }
    // deduce the coordinates of the parent node from the left sibling
    ++level;
    index >>= 1;
    
    return newNode(level, index, isRight(level, index));
  }
  
  

  
  public final N getLeftChild(AbstractNode parent) throws IndexOutOfBoundsException {
    return getLeftChild(parent.level(), parent.index());
  }
  
  public final N getLeftChild(int level, int index) throws IndexOutOfBoundsException {
    Objects.checkFromToIndex(1, level, height());
    return newNode(level - 1, index << 1, false);
  }
  
  
  

  
  public final N getRightChild(AbstractNode parent) throws IndexOutOfBoundsException {
    return getRightChild(parent.level(), parent.index());
  }
  
  public final N getRightChild(int level, int index) throws IndexOutOfBoundsException {
    Objects.checkFromToIndex(1, level, height());
    return getSibling(level - 1, index << 1);
  }
  
  
  
  /**
   * Convenience method.
   * 
   * @see #getSibling(int, int)
   */
  public final N getSibling(AbstractNode node) throws IndexOutOfBoundsException {
    return getSibling(node.level(), node.index());
  }
  
  
  /**
   * 
   * @param level  0 &le; <em>level</em> &lt; {@linkplain #height()}
   * @param index  0 &le; <em>index</em> &lt; {@linkplain #count(int) count(level)}
   */
  public final N getSibling(int level, int index) throws IndexOutOfBoundsException {
    Objects.checkIndex(index, count(level));
    Objects.checkIndex(level, height());
    
    // every odd index joins the node at the index to its left
    if ((index & 1) == 1)
      return newNode(level, index - 1, false);
    
    // index is even; if there's another node to its right, then it joins that one
    if (index < maxIndex(level))
      return newNode(level, index + 1, true);
    
    // index is even, and last
    // we need to find out whether it joins with the (last) node at a level above
    // or below it.
    
    // search below (if it joins below, it joins from the left)
    if (!hasCarry(level)) {
      for (int subLevel = level; subLevel-- > 0; ) {
        if (maxIndexJoinsCarry(subLevel))
          return newNode(subLevel, maxIndex(subLevel), true);
        else if (hasCarry(subLevel))
          break;
      }
    }
    
    // search above (it must now join from the right)
    while (!maxIndexJoinsCarry(++level));
    return newNode(level, maxIndex(level), false);
  }
  
  
  
  /**
   * Determines if the node at the given coordinates is the <em>right</em> child of its parent node.
   * The root level is defined to be left (tho, in principle, it should be undefined).
   * 
   * @param level between zero and <tt>height()</tt> (inclusive)
   * @param index the zero-based node index at the given <tt>level</tt>
   * 
   * @see #maxIndex(int)
   */
  public final boolean isRight(int level, int index) throws IndexOutOfBoundsException {
    Objects.checkIndex(index, count(level));
    // every odd index joins the node at the index to its left (=> is right of index - 1)
    if ((index & 1) == 1)
      return true;
    
    // so, index is even
    // except for the 1 edge case, this joins left, not right
    if (index != maxIndex(level))
      return false;
    

    // index is even, and last.
    
    // we punt on root (defined as left)
    if (level == height()) {
      assert index == 0;
      return false;
    }
    
    // Now, whether left or right depends on how many nodes join carries at the levels below
    // i.e.
    // "how many carries" = # of maxIndexJoinsCarries before hitting hasCarry()
    // (cuz hasCarry(..) represents the collapse / resolution of lower level joined carries)
    int carries = 0;
    for (int v = level; v >= 0; --v) {
      if (maxIndexJoinsCarry(v))
        ++carries;
      if (hasCarry(v))
        break;
    }
    // every 2 dangling nodes (carries) join to form a parent..
    // the node from the lower level is on the right; the node at the higher level, on the left
    switch (carries) {
    case 1:
      // joins upper level node as right
      return true;
    case 2:
      // joins lower level (promoted) node as left
      return false;
    default:
      throw new AssertionError("(" + level + "," + index + "): " + carries);
    }
  }
  
  
  /**
   * Determines if the node at the given coordinates is the <em>left</em> child of its parent node.
   * The root level is defined to be left (tho, in principle, it should be undefined).
   * 
   * @param level between zero and <tt>height()</tt> (inclusive)
   * @param index the zero-based node index at the given <tt>level</tt>
   * 
   * @see #maxIndex(int)
   * @see AbstractNode#isLeft()
   */
  public final boolean isLeft(int level, int index) throws IndexOutOfBoundsException {
    return !isRight(level, index);
  }
  
  
  /**
   * Returns the frontier nodes. If you grow this tree (that is if you append more leaves)
   * then part of new tree will contain exactly the same nodes.
   * Unused meta at this time.
   */
  public final List<N> getFrontier() {
    
    int level = 0;
    int levelCountSansCarry = count();
    
    final int frontierSize = Integer.bitCount(levelCountSansCarry);
    ArrayList<N> frontier = new ArrayList<>(frontierSize);
    
    while (frontier.size() < frontierSize) {
      if ((levelCountSansCarry & 1) == 1) {
        int index = levelCountSansCarry - 1;
        boolean right = isRight(level, index);
        N node = newNode(level, index, right);
        frontier.add(node);
      }
      ++level;
      levelCountSansCarry >>= 1;
    }
    
    return Collections.unmodifiableList(frontier);
  }
  
 
  /**
   * Determines whether an instance is structurally equivalent to another. I.e. it
   * doesn't care about individual node values. As we know, only 1 parameter determines
   * the structure: the leaf {@linkplain #count() count}.
   */
  @Override
  public final boolean equals(Object o) {
    if (this == o)
      return true;
    else if (o instanceof TreeIndex)
      return count() == ((TreeIndex<?>) o).count();
    return false;
  }
  
  
  @Override
  public final int hashCode() {
    return count();
  }
  
  
  @Override
  public String toString() {
    return "TreeIndex(" + count() + ")";
  }
  
  
  /**
   * Returns the height of the Merkle tree root node with <em>count</em>-many leaf elements. (The height
   * of the leaves is zero.)
   * 
   * @param count &ge; 2
   * @return <tt>ceil(log2(<em>count</em>)</tt>
   */
  public static int rootHeightForCount(int count) throws IllegalArgumentException {
    if (count < 2)
      throw new IllegalArgumentException("count (" + count + ") < 2");
    // expected height is ceil(log2(count) which we compute this way..
    return 32 - Integer.numberOfLeadingZeros(count - 1);
  }
  
  
  
  
  
  private N newNode(int level, int index, boolean right) {
    return factory.newNode(level, index, right);
  }
  
  
  
  
  // make this method public static if you should need it outside this class
  private int[] computeLevelCounts(int count) {
    int[] levelCounts = new int[1 + rootHeightForCount(count)];
    int divCount = count;
    levelCounts[0] = divCount;
    int carry = (divCount & 1);
    divCount >>= 1;
    levelCounts[1] = divCount;
    carry += (divCount & 1);
    for (int level = 2; level < levelCounts.length; ++level) {
      divCount >>= 1;
      if (carry == 2) {
        divCount += 1;
        carry = 0;
      }
      levelCounts[level] = divCount;
      carry += (divCount & 1);
    }
    return levelCounts;
  }

}
