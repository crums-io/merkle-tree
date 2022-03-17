/*
 * Copyright 2020-2022 Babak Farhang
 */
package io.crums.util.mrkl;


import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.Test;

/**
 * 
 */
public class FixedLeafTreeTest extends TreeTest {
  
  /**
   * Covers a bug fix.
   */
  @Test
  public void testLeavesBlock() {
    Builder builder = newBuilder();
    byte[] left = { 4, 3, 2, 1 };
    byte[] right = { 1, 2, 3, 4 };
    builder.add(left);
    builder.add(right);
    FixedLeafTree tree = (FixedLeafTree) builder.build();
    ByteBuffer leavesBlock = tree.leavesBlock();
    assertEquals(left.length * 2, leavesBlock.remaining());
    assertEquals(leavesBlock.remaining(), leavesBlock.capacity());
  }

  /* (non-Javadoc)
   * @see io.crums.util.mrkl.TreeTest#newBuilder()
   */
  @Override
  protected FixedLeafBuilder newBuilder() {
    return new FixedLeafBuilder(ALGO, 4);
  }
  
  
  

}
