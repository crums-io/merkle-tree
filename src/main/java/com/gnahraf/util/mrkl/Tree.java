/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util.mrkl;


import java.security.MessageDigest;
import java.util.Objects;

import com.gnahraf.util.mrkl.index.TreeIndex;

/**
 * 
 */
public class Tree {
  
  // Static hashing support
  
  public final static byte LEAF_PAD = 0;
  public final static byte BRANCH_PAD = 1;
  
  
  public static byte[] hashLeaves(byte[] left, byte[] right, MessageDigest digest) {
    return hashCommon(left, right, digest, LEAF_PAD);
  }
  
  public static byte[] hashInternals(byte[] left, byte[] right, MessageDigest digest) {
    byte[] hash = hashCommon(left, right, digest, BRANCH_PAD);
    
    // sanity check
    if (left.length != hash.length)
      throw new IllegalArgumentException(
          "digest/left length mismatch: " + hash.length +
          " (" + digest.getAlgorithm() + ") / " + left.length);
    if (right.length != hash.length)
      throw new IllegalArgumentException(
          "digest/right length mismatch: " + hash.length +
          " (" + digest.getAlgorithm() + ") / " + right.length);
    
    return hash;
  }
  
  public static byte[] hashUncommon(byte[] leftInternal, byte[] rightLeaf, MessageDigest digest) {
    checkArgs(leftInternal, rightLeaf, digest);
    
    if (leftInternal.length != digest.getDigestLength())
      throw new IllegalArgumentException(
          "digest/hash length mismatch: " + digest.getDigestLength() +
          " (" + digest.getAlgorithm() + ") / " + leftInternal.length);
    
    digest.reset();
    digest.update(BRANCH_PAD);
    digest.update(leftInternal);
    digest.update(LEAF_PAD);
    digest.update(rightLeaf);
    return digest.digest();
  }
  
  
  private static void checkArgs(byte[] left, byte[] right, MessageDigest digest) {
    Objects.requireNonNull(left, "left");
    Objects.requireNonNull(right, "right");
    Objects.requireNonNull(digest, "digest");
  }
  
  
  private static byte[] hashCommon(byte[] left, byte[] right, MessageDigest digest, byte padding) {
    checkArgs(left, right, digest);
    digest.reset();
    digest.update(padding);
    digest.update(left);
    digest.update(padding);
    digest.update(right);
    return digest.digest();
  }
  
  
  
  
  
  
  
  
  
  
  

  private final String algo;
  private final TreeIndex<Node> idx;
  private final byte[][] data;

  /**
   * 
   */
  public Tree(byte[][] data, int leaves, String algo) {
    this(data, leaves, algo, true);
  }
  
  
  Tree(byte[][] data, int leaves, String algo, boolean copy) {
    this.algo = Objects.requireNonNull(algo, "algo");
    Objects.requireNonNull(data, "data");
    this.idx = new TreeIndex<>(leaves, new NodeFactory());
    
    if (data.length != idx.totalCount())
      throw new IllegalArgumentException(
          "expected " + idx.totalCount() + " data elements for " +
              leaves + " many leaves; only " + data.length + " are given");
    this.data = copy ? deepCopy(data) : data;
    
  }
  
  
  public Node root() {
    return idx.getNode(0);
  }
  
  
  /**
   * @see Node
   */
  TreeIndex<Node> idx() {
    return idx;
  }
  
  /**
   * @see Node#data()
   */
  byte[] data(int level, int index) {
    int serialIndex = idx.serialIndex(level, index);
    return data[serialIndex];
  }
  
  
  
  
  private byte[][] deepCopy(byte[][] data) {
    byte[][] copy = new byte[data.length][];
    for (int index = data.length; index-- > 0; )
      copy[index] = copy(data[index]);
    return copy;
  }
  
  private byte[] copy(byte[] array) {
    byte[] copy = new byte[array.length];
    for (int index = array.length; index-- > 0;)
      copy[index] = array[index];
    return copy;
  }
  
  
  private class NodeFactory implements TreeIndex.NodeFactory<Node> {

    @Override
    public Node newNode(int level, int index, boolean right) {
      return new Node(level, index, Tree.this);
    }
    
  }

}
