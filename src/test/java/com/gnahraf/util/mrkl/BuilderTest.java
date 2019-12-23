/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util.mrkl;


import static org.junit.Assert.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import org.junit.Test;


/**
 * Tests Builder along with Node and Tree classes, since the 3 are interdependent.
 */
public class BuilderTest {
  
  public final static String ALGO = "SHA-256";
  
  
  @Test
  public void testEmpty() {
    Builder builder = newBuilder();
    assertEquals(0, builder.count());
    assertEquals(ALGO, builder.getHashAlgo());
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
    int minDataLen = 3;
    int maxDataLen = 16;
    int dataLenRange = 1 + maxDataLen - minDataLen;
    
    Random rand = new Random(12);
    byte[][] items = new byte[count][];
    for (int i = 0; i < count; ++i) {
      int dataLen = minDataLen + rand.nextInt(dataLenRange);
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
  
  private void assertHash(Node node, MessageDigest digest) {
    assertHash(node, digest, false);
  }
  
  private void assertHashRecurse(Node node, MessageDigest digest) {
    assertHash(node, digest, true);
  }
  
  
  private void assertHash(Node node, MessageDigest digest, boolean recurse) {
    if (node.isLeaf())
      return;
    
    digest.reset();
    
    Node left = node.leftChild();
    Node right = node.rightChild();

//    System.out.println();
//    System.out.println("P " + node);
//    System.out.println("L " + left);
//    System.out.println("R " + right);
    
    //  quick structural checks (tho other unit tests devoted to that)
    if (left.isLeaf())
      assertTrue(right.isLeaf());
    assertEquals(node.level() - 1, left.level());
    
    if (node.isCarry()) {
      // structural
      assertTrue(left.level() > right.level());
      assertFalse(left.isLeaf());
      
      digest.update(Tree.BRANCH_PAD);
      digest.update(left.data());
      digest.update(right.isLeaf() ? Tree.LEAF_PAD : Tree.BRANCH_PAD);
      digest.update(right.data());
    } else {
      // structural
      assertEquals(node.level() - 1, right.level());
      
      byte pad = left.isLeaf() ? Tree.LEAF_PAD : Tree.BRANCH_PAD;
      
      digest.update(pad);
      digest.update(left.data());
      digest.update(pad);
      digest.update(right.data());
    }
    
    assertArrayEquals(digest.digest(), node.data());
    
    if (recurse) {
      // pre-order traversal
      assertHash(left, digest, true);
      assertHash(right, digest, true);
    }
  }
  
  private Builder newBuilder() {
    return new Builder(ALGO);
  }
  
  
  private MessageDigest newDigest() {
    try {
      return MessageDigest.getInstance(ALGO);
    } catch (NoSuchAlgorithmException nsax) {
      fail(nsax.getMessage());
      return null;
    }
  }

}
