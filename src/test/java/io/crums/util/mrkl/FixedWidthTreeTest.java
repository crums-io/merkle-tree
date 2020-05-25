/*
 * Copyright 2020 Babak Farhang
 */
package io.crums.util.mrkl;



import static org.junit.Assert.*;

import java.nio.ByteBuffer;

import org.junit.Test;

import io.crums.util.mrkl.Builder;
import io.crums.util.mrkl.FixedWidthTree;

/**
 * 
 */
public class FixedWidthTreeTest extends TreeTest {
  
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
    FixedWidthTree tree = (FixedWidthTree) builder.build();
    ByteBuffer leavesBlock = tree.leavesBlock();
    assertEquals(left.length * 2, leavesBlock.remaining());
    assertEquals(leavesBlock.remaining(), leavesBlock.capacity());
  }

}
