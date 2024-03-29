/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.util.mrkl;


import static io.crums.util.mrkl.intenal.Bytes.copy;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * A more compact {@code Tree} appropriate if the leaves are fixed-width
 * and it'all fits under 2 gigabytes.
 * 
 * @see #treeDataLength(int, int, int)
 */
public class FixedLeafTree extends Tree {
  
  public final static int MIN_ALGO_WIDTH = 8;
  public final static int MIN_LEAF_WIDTH = 1;
  
  private final byte[] data;
  private final int algoWidth;
  private final int leafWidth;
  private final int levelZeroOffset;

  /**
   * Creates a new instance.
   * 
   * @param leaves    number of leaf nodes in the tree   
   * @param algo      hash algo used for the trees internal nodes
   * @param data      node data in serial form (interpreted by next parameters)
   * @param algoWidth number of bytes in the hash generated by <code>algo</code>
   * @param leafWidth number of bytes in a leaf node
   */
  public FixedLeafTree(int leaves, String algo, byte[] data, int algoWidth, int leafWidth)
      throws IllegalArgumentException {
    super(leaves, algo);
    
    this.data = Objects.requireNonNull(data, "data");
    this.algoWidth = algoWidth;
    this.leafWidth = leafWidth;

    validateArgs(algoWidth, leafWidth);
    
    {
      long zOff = (idx().totalCount() - leaves) * ((long) algoWidth);
      if (zOff >= Integer.MAX_VALUE)
        throw new IllegalArgumentException("data provably too short");
      levelZeroOffset = (int) zOff;
    }
    
    if (data.length < levelZeroOffset + leaves*((long) leafWidth))
      throw new IllegalArgumentException("data too short");
  }
  
  
  /**
   * Copy constructor.
   */
  protected FixedLeafTree(FixedLeafTree copy) {
    super(copy);
    this.data = copy.data;
    this.algoWidth = copy.algoWidth;
    this.leafWidth = copy.leafWidth;
    this.levelZeroOffset = copy.levelZeroOffset;
  }
  

  @Override
  public byte[] data(int level, int index) {
    if (level == 0) {
      int offset = levelZeroOffset + index*leafWidth;
      return copy(data, offset, leafWidth);
    } else {
      int offset = idx().serialIndex(level, index) * algoWidth;
      return copy(data, offset, algoWidth);
    }
  }
  
  
  /**
   * Returns the fixed-size leaf width.
   * 
   * @return in bytes
   */
  @Override
  public int leafWidth() {
    return leafWidth;
  }
  
  
  /**
   * Returns entire data block.
   * 
   * @return a new <em>read-only</em> view of the entire block.
   */
  public ByteBuffer dataBlock() {
    return ByteBuffer.wrap(data).asReadOnlyBuffer();
  }
  
  
  /**
   * Returns the leaves' data block.
   * 
   * @return a new <em>read-only</em> view of leaves' block.
   */
  public ByteBuffer leavesBlock() {
    return ByteBuffer.wrap(data, levelZeroOffset, leafWidth * idx().count()).slice().asReadOnlyBuffer();
  }
  
  
  /**
   * Returns the data beyond the standard definition of the tree. This is a view of whatever bytes
   * remain in the {@linkplain #dataBlock() data block} after the tree definition.
   * 
   * @return possibly empty extra block
   */
  protected ByteBuffer extraBlock() {
    int startIndex = treeDataLength(idx().count(), algoWidth, leafWidth);
    return ByteBuffer.wrap(data, startIndex, data.length - startIndex).slice().asReadOnlyBuffer();
  }
  
  
  public int hashWidth() {
    return algoWidth;
  }
  
  
  
  public static boolean fitsModelCapacity(int leaves, int algoWidth, int leafWidth) {
    return treeDataLength(leaves, algoWidth, leafWidth) > 0;
  }
  
  
  /**
   * Returns the number of bytes needed to encode the state of the tree, or -1 if that
   * number exceeds the maximum {@code int} Java allows.
   */
  public static int treeDataLength(int leaves, int algoWidth, int leafWidth) {
    if (leaves < 2)
      throw new IllegalArgumentException("leaves (" + leaves + ") < 2");
    validateArgs(algoWidth, leafWidth);
    long bytes = ((long) leaves) * leafWidth;
    bytes += ((long) leaves - 1) * algoWidth;
    return bytes <= Integer.MAX_VALUE ? (int) bytes : -1;
  }
  
  
  private static void validateArgs(int algoWidth, int leafWidth) {
    
    if (algoWidth < MIN_ALGO_WIDTH)
      throw new IllegalArgumentException("algoWidth (" + algoWidth + ") < " + MIN_ALGO_WIDTH);
    if (leafWidth < MIN_LEAF_WIDTH)
      throw new IllegalArgumentException("leafWidth (" + leafWidth + ") < " + MIN_LEAF_WIDTH);
    
  }

}
