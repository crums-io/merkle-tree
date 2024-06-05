/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.util.mrkl;


import static org.junit.jupiter.api.Assertions.*;

import java.security.MessageDigest;
import java.util.Random;

import org.junit.jupiter.api.Test;

/**
 * 
 */
public class ProofTest extends TreeTest {
  
  
  @Test
  public void testMinimal() {
    Builder builder = newBuilder();
    byte[] left = "gnahraf".getBytes();
    byte[] right = "kabab".getBytes();
    builder.add(left);
    builder.add(right);
    assertEquals(2, builder.count());
    Tree tree = builder.build();
    
    try {
      new Proof(tree, 2);
      fail();
    } catch (IndexOutOfBoundsException expected) {  }
    

    int index = 0;
    Proof proof = new Proof(tree, index);
    assertEquals(index, proof.leafIndex());
    assertEquals(tree.idx().count(), proof.leafCount());
    assertEquals(3, proof.hashChain().size());
    assertArrayEquals(tree.root().data(), proof.rootHash());

    Node node = tree.idx().getNode(0, index);
    assertArrayEquals(left, node.data());
    assertArrayEquals(node.data(), proof.item());
    assertArrayEquals(right, node.sibling().data());
    assertArrayEquals(node.sibling().data(), proof.hashChain().get(1));
    assertTrue(proof.verify(newDigest()));
  }
  
  
  @Test
  public void testTinies() {
    for (int count = 3; count < 132; ++ count)
      testRandom(count);
  }
  
  
  @Test
  public void testSmall() {
    testRandom(517);
  }
  
  
  @Test
  public void testSmallRange() {
    for (int count = 500; count < 533; ++count)
      testRandom(count);
  }
  
  
  @Test
  public void testModerate() {
    testRandom(512*1024 - 19);
  }
  
  
  
  
  private void testRandom(int count) {
    Tree tree = newRandomTree(count, 16, 48);
    MessageDigest digest = newDigest();
    for (int leaf = 0; leaf < count; ++leaf) {
      Proof proof = new Proof(tree, leaf);
      assertArrayEquals(tree.data(0, leaf), proof.item());
      assertArrayEquals(tree.root().data(), proof.rootHash());
      assertTrue(proof.verify(digest));
    }
  }
  
  

  Tree newRandomTree(int count, int minDataLen, int maxDataLen) {
    int dataLenRange = 1 + maxDataLen - minDataLen;
    Builder builder = newBuilder();
    Random rand = new Random(7);
    for (int i = 0; i < count; ++i) {
      int dataLen = minDataLen + rand.nextInt(dataLenRange);
      byte[] data = new byte[dataLen];
      rand.nextBytes(data);
      builder.add(data);
    }
    return builder.build();
  }
  
}
