/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util.mrkl;


import static com.gnahraf.util.mem.Bytes.copy;

import java.util.Objects;

/**
 * A more compact <tt>Tree</tt> appropriate if the leaves are fixed-width
 * and it'all fits under a gigabyte.
 * 
 * @see #dataLength(int, int, int)
 */
public class FixedWidthTree extends Tree {
  
  public final static int MIN_ALGO_WIDTH = 8;
  public final static int MIN_LEAF_WIDTH = 1;
  
  private final byte[] data;
  private final int algoWidth;
  private final int leafWidth;
  private final int levelZeroOffset;

  /**
   * Creates a new instance.
   * 
   * @param leaves      
   * @param algo
   * @param data
   * @param algoWidth
   * @param leafWidth
   * @throws IllegalArgumentException
   */
  public FixedWidthTree(int leaves, String algo, byte[] data, int algoWidth, int leafWidth)
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
  
  
  
  public static boolean fitsModelCapacity(int leaves, int algoWidth, int leafWidth) {
    return dataLength(leaves, algoWidth, leafWidth) > 0;
  }
  
  
  /**
   * Returns the number of bytes needed to encode the state of the tree, or -1 if that
   * number exceeds the maximum <tt>int</tt> Java allows.
   */
  public static int dataLength(int leaves, int algoWidth, int leafWidth) {
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