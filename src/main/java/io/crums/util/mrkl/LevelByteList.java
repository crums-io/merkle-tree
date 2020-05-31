/*
 * Copyright 2020 Babak Farhang
 */
package io.crums.util.mrkl;

import java.util.AbstractList;
import java.util.Objects;
import java.util.RandomAccess;

/**
 * Read-only <tt>List<byte[]></tt> implementation using node data at a specific
 * level in a {@linkplain Tree}. The use case here is to build a bigger tree,
 * or perhaps just to rebuild its carries (nodes added to stitch up a tree's frontier
 * nodes and buiid a root). Since in that case the <em>carry</em> nodes in the base
 * tree are never consulted, the default behavior of this class is to drop the
 * carry node (which is necessarily at the highest index) from the list.
 */
class LevelByteList extends AbstractList<byte[]> implements RandomAccess {
  
  private final Tree tree;
  private final int level;
  private final int size;

  /**
   * Creates an instance that does not include the carry node if present
   * at the specified <tt>level</tt>.
   */
  public LevelByteList(Tree tree, int level) throws IndexOutOfBoundsException {
    this(tree, level, false);
  }

  /**
   * Creates a new instance from node data in the Merkle tree at the specified
   * <tt>level</tt>.
   */
  public LevelByteList(Tree tree, int level, boolean includeCarry) throws IndexOutOfBoundsException {
    this.tree = Objects.requireNonNull(tree, "tree");
    this.level = level;
    
    if (includeCarry || !tree.idx().hasCarry(level))
      this.size = tree.idx().count(level);
    else
      this.size = tree.idx().count(level) - 1;
  }

  /**
   * <p>Returns the data for the node at the given <tt>index</tt> at this {@linkplain #level() level}
   * in the {@linkplain #tree() tree}.</p>
   * 
   * {@inheritDoc}
   * 
   * @return {@linkplain Tree#data(int, int)}
   */
  @Override
  public byte[] get(int index) {
    Objects.checkIndex(index, size);
    return tree.data(level, index);
  }
  
  /**
   * Returns the level in the tree this instance represents.
   * (Unused meta.)
   */
  public final int level() {
    return level;
  }
  
  /**
   * Returns the source of this instance.
   * (Unused meta.)
   */
  public final Tree tree() {
    return tree;
  }

  
  @Override
  public int size() {
    return size;
  }

}
