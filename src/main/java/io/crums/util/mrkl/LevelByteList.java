/*
 * Copyright 2020 Babak Farhang
 */
package io.crums.util.mrkl;

import java.util.AbstractList;
import java.util.Objects;
import java.util.RandomAccess;

/**
 * Read-only <tt>List<byte[]></tt> implementation using node data at a specific
 * level in a {@linkplain Tree}.
 */
class LevelByteList extends AbstractList<byte[]> implements RandomAccess {
  
  private final Tree tree;
  private final int level;
  private final int size;

  /**
   * 
   */
  public LevelByteList(Tree tree, int level) throws IndexOutOfBoundsException {
    this(tree, level, false);
  }

  /**
   * Creates a new instance from node data in the Merkle tree at the 
   * @param tree
   * @param level
   * @param includeCarry
   * @throws IndexOutOfBoundsException
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
