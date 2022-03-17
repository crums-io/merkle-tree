/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.util.mrkl;


import static org.junit.jupiter.api.Assertions.*;

import java.security.MessageDigest;
import java.util.Random;

import org.junit.jupiter.api.Test;


/**
 * Tests Builder along with Node and Tree classes, since the 3 are interdependent.
 */
public class BuilderTest extends TreeTest {
  
  
  @Test
  public void testEmpty() {
    Builder builder = newBuilder();
    assertEquals(0, builder.count());
    assertEquals(algo, builder.getHashAlgo());
    try {
      builder.build();
      fail();
    } catch (IllegalStateException expected) {  }
  }
  
  
  @Test
  public void testOne() {
    Builder builder = newBuilder();
    byte[] data = { 4, 3, 2, 1 };
    builder.add(data);
    assertEquals(1, builder.count());
    
    try {
      builder.build();
      fail();
    } catch (IllegalStateException expected) {  }
  }
  
  
  @Test
  public void testMinimal() {
    Builder builder = newBuilder();
    byte[] left = { 4, 3, 2, 1 };
    byte[] right = { 1, 2, 3, 4 };
    builder.add(left);
    builder.add(right);
    assertEquals(2, builder.count());
    Tree tree = builder.build();
    assertEquals(ALGO, tree.getHashAlgo());
    Node root = tree.root();
    assertTrue(root.isRoot());
    assertEquals(1, root.level());
    assertEquals(2, root.leafCount());
    assertNull(root.sibling());
    
    Node leftLeaf = root.leftChild();
    assertTrue(leftLeaf.isLeaf());
    assertFalse(leftLeaf.isRoot());
    assertTrue(leftLeaf.isLeft());
    assertArrayEquals(left, leftLeaf.data());
    
    Node rightLeaf = root.rightChild();
    assertTrue(rightLeaf.isLeaf());
    assertTrue(rightLeaf.isRight());
    assertArrayEquals(right, rightLeaf.data());
    
    MessageDigest digest = newDigest();
    digest.update(Tree.LEAF_PAD);
    digest.update(left);
    digest.update(Tree.LEAF_PAD);
    digest.update(right);
    
    assertArrayEquals(digest.digest(), root.data());
    
    // test the test
    byte[][] items = { left, right };
    assertTree(items, tree);
    assertHash(root, newDigest());
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

//    System.out.println();
//    System.out.println("+++");
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
  public void test09_F() {
    byte[][] items = {
        { 0, 2, 3, 1 },
        { 5, 6, 7, 8 },
        { 9, 0xa, 0xb, 0xc},
        { 0xd, 0xe, 0xf, 0x10},
        { 0x11, 0x12, 0x13, 9 },
        { 0x12, 0x14, 0x70, 0x6e },
        { 5, 6, 7, 10 },
        { 0, 0x1b, 0x55, 0x2d },
        { 12, -12, -128, 127 },
    };
    testItems(items);
  }
  
  
  @Test
  public void testBig1M() {
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
  public void testBig1M_1() {
    int count = 1024*1024 - 1;
//    int minDataLen = 3;
//    int maxDataLen = 16;
    int dataLen = 127;
//    int dataLenRange = 1 + maxDataLen - minDataLen;
    
    Random rand = new Random(12);
    byte[][] items = new byte[count][];
    for (int i = 0; i < count; ++i) {
//      int dataLen = minDataLen + rand.nextInt(dataLenRange);
      byte[] data = new byte[dataLen];
      rand.nextBytes(data);
      items[i] = data;
    }
    testItems(items);
  }
  
  
  
  
  
  private void testItems(byte[][] items) {
    Builder builder = newBuilder();
    for (byte[] item : items) {
      builder.add(item);
    }
    Tree tree = builder.build();
//    System.out.println(tree);
    assertTree(items, tree);
  }
  
  
  private void assertTree(byte[][] items, Tree tree) {
    assertEquals(items.length, tree.root().leafCount());
    assertEquals(2 * items.length - 1, tree.idx().totalCount());
    assertHashRecurse(tree.root(), newDigest());
    Node root = tree.root();
    for (int index = 0; index < items.length; ++index)
      assertArrayEquals(items[index], root.getLeaf(index).data());
  }
  
  
  
  
  
  
}
