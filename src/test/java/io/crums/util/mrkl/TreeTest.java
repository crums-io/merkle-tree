/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.util.mrkl;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import io.crums.util.mrkl.Builder;
import io.crums.util.mrkl.Node;
import io.crums.util.mrkl.Tree;

/**
 * (Factored out test code.)
 */
abstract class TreeTest {
  
  public final static String ALGO = "SHA-256";
  
  public final String algo;
  
  protected TreeTest() {
    this(ALGO);
  }

  /**
   * 
   */
  protected TreeTest(String algo) {
    this.algo = algo;
  }
  
  
  
  
  protected MessageDigest newDigest() {
    try {
      return MessageDigest.getInstance(algo);
    } catch (NoSuchAlgorithmException nsax) {
      fail(nsax.getMessage());
      return null;
    }
  }
  

  
  protected Builder newBuilder() {
    return new Builder(algo);
  }
  

  
  
  public static void assertHash(Node node, MessageDigest digest) {
    assertHash(node, digest, false);
  }
  
  public static void assertHashRecurse(Node node, MessageDigest digest) {
    assertHash(node, digest, true);
  }
  
  
  private static void assertHash(Node node, MessageDigest digest, boolean recurse) {
    
    node.verify(digest);
    
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


}
