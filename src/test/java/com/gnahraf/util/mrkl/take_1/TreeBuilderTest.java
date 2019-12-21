/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util.mrkl.take_1;


import static org.junit.Assert.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

import com.gnahraf.util.mrkl.take_1.Level;
import com.gnahraf.util.mrkl.take_1.Node;
import com.gnahraf.util.mrkl.take_1.Tree;
import com.gnahraf.util.mrkl.take_1.TreeBuilder;

/**
 * 
 */
public class TreeBuilderTest {
  
  public final static String ALGO = "SHA-256";
  
  
  @Test
  public void test00Empty() {
    TreeBuilder builder = newBuilder();
    assertEquals(0, builder.count());
    try {
      builder.build();
      fail();
    } catch (IllegalStateException expected) {  }
  }
  
  @Test
  public void test01One() {
    TreeBuilder builder = newBuilder();
    byte[] data = { 4, 3, 2, 1 };
    builder.add(data);
    assertEquals(1, builder.count());
    
    try {
      builder.build();
      fail();
    } catch (IllegalStateException expected) {  }
    
  }
  
  
  @Test
  public void test02Minimal() throws Exception {
    TreeBuilder builder = newBuilder();
    byte[] left = { 4, 3, 2, 1 };
    byte[] right = { 1, 2, 3, 4 };
    builder.add(left);
    builder.add(right);
    assertEquals(2, builder.count());
    Tree tree = builder.build();
    assertEquals(ALGO, tree.getAlgo());
    assertEquals(1, tree.getRoot().height());
    assertEquals(2, tree.getCount());
    MessageDigest digest = MessageDigest.getInstance(ALGO);
    assertTrue(tree.getRoot().verify(digest));
    tree.getRoot().ensure(digest);
    assertTrue(Arrays.equals(left, tree.getRoot().getLeft().getHash()));
    assertTrue(Arrays.equals(right, tree.getRoot().getRight().getHash()));
    assertEquals(tree.getRoot().getLeft(), tree.getLeaves().get(0));
    assertEquals(tree.getRoot().getRight(), tree.getLeaves().get(1));
  }
  
  
  
  @Test
  public void test03() {
    byte[][] items = {
        { 1, 2, 3, 4 },
        { 5, 6, 7, 8 },
        { 9, 0xa, 0xb, 0xc},
    };
    testItems(items);
  }
  
  
  
  @Test
  public void test04() {
    byte[][] items = {
        { 0, 1, 2, 3, 4 },
        { 5, 6, 7, 8 },
        { 9, 0xa, 0xb, 0xc},
        { 0xd, 0xe, 0xf, 0x10},
    };
    testItems(items);
  }
  
  
  
  @Test
  public void test05() {
    byte[][] items = {
        { 0, 1, 2, 3, 4 },
        { 5, 6, 7, 8 },
        { 9, 0xa, 0xb, 0xc},
        { 0xd, 0xe, 0xf, 0x10},
        { 0x11, 0x12, 0x13 },
    };
    testItems(items);
  }
  
  
  
  @Test
  public void test06() {
    byte[][] items = {
        { 0, 1, 2, 3, 4 },
        { 5, 6, 7, 8 },
        { 9, 0xa, 0xb, 0xc},
        { 0xd, 0xe, 0xf, 0x10},
        { 0x11, 0x12, 0x13 },
        { 0x11, 0x12, 0x14, 0x70, 0x6e },
    };
    testItems(items);
  }
  
  
  /**
   * (There's a carry at each level, except root)
   */
  @Test
  public void test07() {
    byte[][] items = {
        { 0, 1, 2, 3, 4 },
        { 5, 6, 7, 8 },
        { 9, 0xa, 0xb, 0xc},
        { 0xd, 0xe, 0xf, 0x10},
        { 0x11, 0x12, 0x13 },
        { 0x11, 0x12, 0x14, 0x70, 0x6e },
        { 0, 0x1b, 0x3f, 0x55, 0x2d },
    };
    testItems(items);
  }
  
  
  
  @Test
  public void test08() {
    byte[][] items = {
        { 0, 1, 2, 3, 4 },
        { 5, 6, 7, 8 },
        { 9, 0xa, 0xb, 0xc},
        { 0xd, 0xe, 0xf, 0x10},
        { 0x11, 0x12, 0x13 },
        { 0x11, 0x12, 0x14, 0x70, 0x6e },
        { 5, 6, 7, 10 },
        { 0, 0x1b, 0x3f, 0x55, 0x2d },
    };
    testItems(items);
  }
  
  
  
  @Test
  public void test09() {
    byte[][] items = {
        { 0, 1, 2, 3, 4 },
        { 5, 6, 7, 8 },
        { 9, 0xa, 0xb, 0xc},
        { 0xd, 0xe, 0xf, 0x10},
        { 0x11, 0x12, 0x13 },
        { 0x11, 0x12, 0x14, 0x70, 0x6e },
        { 5, 6, 7, 10 },
        { 0, 0x1b, 0x3f, 0x55, 0x2d },
        { 12, -12, -128, 127 },
    };
    testItems(items);
  }
  
  
  @Test
  public void test10Big() {
    int count = 1024*1024;
    int minDataLen = 3;
    int maxDataLen = 16;
    int dataLenRange = 1 + maxDataLen - minDataLen;
    
    Random rand = new Random(11);
    byte[][] items = new byte[count][];
    for (int i = 0; i < count; ++i) {
      int dataLen = minDataLen + rand.nextInt(dataLenRange);
      byte[] data = new byte[dataLen];
      rand.nextBytes(data);
      items[i] = data;
    }
    testItems(items);
  }
  
  
  @Test
  public void test11Big() {
    int count = 1024*1024 - 1;
    int minDataLen = 3;
    int maxDataLen = 16;
    int dataLenRange = 1 + maxDataLen - minDataLen;
    
    Random rand = new Random(11);
    byte[][] items = new byte[count][];
    for (int i = 0; i < count; ++i) {
      int dataLen = minDataLen + rand.nextInt(dataLenRange);
      byte[] data = new byte[dataLen];
      rand.nextBytes(data);
      items[i] = data;
    }
    testItems(items);
  }
  
  
  /**
   * Returns the expected height of a Merkle tree for the given number of leaves.
   * 
   * @param count &gt; 1
   */
  public static int expectedHeightForCount(int count) {
    // expected height is ceil(log2(count) which we compute this way..
    return 32 - Integer.numberOfLeadingZeros(count - 1);
  }
  
  
  private void testItems(byte[][] items) {
    TreeBuilder builder = newBuilder();
    for (byte[] item : items) {
      builder.add(item);
    }
    Tree tree = builder.build();
    assertTree(items, tree);
  }
  
  
  
  private void assertTree(byte[][] items, Tree tree) {
    assertEquals(items.length, tree.getCount());
    // expected height is ceil(log2(items.length) which we compute this way..
    int expectedHeight = expectedHeightForCount(items.length);
    
    assertEquals(expectedHeight, tree.getRoot().height());
    for (int i = 0; i < items.length; ++i)
      assertTrue(Arrays.equals(items[i], tree.getLeaves().get(i).getHash()));
    Level[] levels = new Level[expectedHeight + 1];
    levels[expectedHeight] = new Level(tree);
    for (int i = expectedHeight; i-- > 0; ) {
      levels[i] = new Level(levels[i + 1]);
    }
    assertTrue(levels[0].isLeaves());
    assertEquals(tree.getLeaves(), levels[0].nodes());
    MessageDigest digest = newDigest();
    for (Level level : levels)
      for (Node node : level.nodes()) {
        node.ensure(digest);
        assertEquals(level.level(), node.height());
      }
  }
  
  
  
  
  protected TreeBuilder newBuilder() {
    return new TreeBuilder(ALGO);
  }
  
  
  public static MessageDigest newDigest() {
    try {
      return MessageDigest.getInstance(ALGO);
    } catch (NoSuchAlgorithmException nsax) {
      fail(nsax.getMessage());
      return null;
    }
  }

}
