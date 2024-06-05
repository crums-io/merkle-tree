/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.util.mrkl;


import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Objects;

import io.crums.util.mrkl.index.TreeIndex;

/**
 * The Merkle tree. Instances are immutable. This class abstracts away the
 * memory layout for the tree.
 * 
 * @see Builder
 * @see FixedLeafTree
 * @see FreeLeafTree
 */
public abstract class Tree {
  
  // Static hashing support
  
  public final static byte LEAF_PAD = 0;
  public final static byte BRANCH_PAD = 1;
  
  
  public static byte[] hashLeaves(
      byte[] left, byte[] right, MessageDigest digest) {

    checkArgs(left, right, digest);
    return hashLeftRight(left, right, digest, LEAF_PAD);
  }


  /**
   * Computes and returns the hash of the given leaf nodes. On return,
   * the given buffers have no remaining bytes.
   */
  public static byte[] hashLeaves(
      ByteBuffer left, ByteBuffer right, MessageDigest digest) {
        
    checkArgs(left, right, digest);
    return hashLeftRight(left, right, digest, LEAF_PAD);
  }

  
  
  public static byte[] hashInternals(
      byte[] left, byte[] right, MessageDigest digest) {

    byte[] hash = hashLeftRight(left, right, digest, BRANCH_PAD);
    
    // sanity check
    if (left.length != hash.length)
      throw new IllegalArgumentException(
          "digest length/left length mismatch: " + hash.length +
          " (" + digest.getAlgorithm() + ") / " + left.length);
    if (right.length != hash.length)
      throw new IllegalArgumentException(
          "digest length/right length mismatch: " + hash.length +
          " (" + digest.getAlgorithm() + ") / " + right.length);
    
    return hash;
  }
  


  /**
   * Computes and returns the hash of the given merkle nodes. On return,
   * the given buffers have no remaining bytes.
   */
  public static byte[] hashInternals(
      ByteBuffer left, ByteBuffer right, MessageDigest digest) {
        
    final int lr = left.remaining(), rr = right.remaining();
    byte[] hash = hashLeftRight(left, right, digest, BRANCH_PAD);
    
    // sanity check
    if (lr != hash.length)
      throw new IllegalArgumentException(
          "digest length /left remaining mismatch: " + hash.length +
          " (" + digest.getAlgorithm() + ") / " + lr);
    if (rr != hash.length)
      throw new IllegalArgumentException(
          "digest length /right remaining mismatch: " + hash.length +
          " (" + digest.getAlgorithm() + ") / " + rr);
    
    return hash;
  }



  

  /**
   * Computes and returns the hash of the given merkle nodes.
   */
  public static byte[] hashUncommon(
      byte[] leftInternal, byte[] rightLeaf, MessageDigest digest) {
    checkArgs(leftInternal, rightLeaf, digest);
    
    if (leftInternal.length != digest.getDigestLength())
      throw new IllegalArgumentException(
          "digest length / leftInternal length mismatch: " +
          digest.getDigestLength() +
          " (" + digest.getAlgorithm() + ") / " + leftInternal.length);
    
    digest.reset();
    digest.update(BRANCH_PAD);
    digest.update(leftInternal);
    digest.update(LEAF_PAD);
    digest.update(rightLeaf);
    return digest.digest();
  }


  /**
   * Computes and returns the hash of the given merkle nodes. On return,
   * the given buffers have no remaining bytes.
   */
  public static byte[] hashUncommon(
      ByteBuffer leftInternal, ByteBuffer rightLeaf, MessageDigest digest) {
    checkArgs(leftInternal, rightLeaf, digest);
    
    if (leftInternal.remaining() != digest.getDigestLength())
      throw new IllegalArgumentException(
          "digest length / leftInternal remaining mismatch: " +
          digest.getDigestLength() +
          " (" + digest.getAlgorithm() + ") / " + leftInternal.remaining());
    
    digest.reset();
    digest.update(BRANCH_PAD);
    digest.update(leftInternal);
    digest.update(LEAF_PAD);
    digest.update(rightLeaf);
    return digest.digest();
  }
  
  
  private static void checkArgs(Object left, Object right, MessageDigest digest) {
    Objects.requireNonNull(left, "left");
    Objects.requireNonNull(right, "right");
    Objects.requireNonNull(digest, "digest");
  }
  
  
  private static byte[] hashLeftRight(
      byte[] left, byte[] right, MessageDigest digest, byte padding) {
    
    // checkArgs(left, right, digest);
    digest.reset();
    digest.update(padding);
    digest.update(left);
    digest.update(padding);
    digest.update(right);
    return digest.digest();
  }


  
  
  private static byte[] hashLeftRight(
      ByteBuffer left, ByteBuffer right, MessageDigest digest, byte padding) {
    
    // checkArgs(left, right, digest);
    digest.reset();
    digest.update(padding);
    digest.update(left);
    digest.update(padding);
    digest.update(right);
    return digest.digest();
  }

  
  
  
  
  
  
  // Instance definition follows..
  

  private final String algo;
  private final TreeIndex<Node> idx;
  
  
  /**
   * Base constructor creates an index and locks down the algo.
   * 
   * @param leaves  the number of leaf nodes in the tree
   * @param algo    the hash algo used for the trees internal nodes.
   */
  protected Tree(int leaves, String algo) {
    this.algo = Objects.requireNonNull(algo, "algo");
    this.idx = new TreeIndex<>(leaves, new NodeFactory());
  }
  
  /**
   * Copy constructor.
   */
  protected Tree(Tree copy) {
    this.algo = copy.algo;
    this.idx = copy.idx;
  }
  
  
  /**
   * Returns the root node of the tree.
   * 
   * @see #hash()
   */
  public final Node root() {
    return idx.getNode(0);
  }
  
  
  /**
   * Returns the hash of the root of the tree.
   */
  public final byte[] hash() {
    return data(idx.height(), 0);
  }
  
  
  /**
   * Returns the hashing algorithm that the tree's internal nodes use.
   * The {@linkplain #data(int, int) data} for internal nodes (level &gt; 0) is a hash whose value is
   * computed using this algorithm. (The data for leaf nodes can be anything and is independent
   * of this algorithm.)
   * 
   * @return a name compatible with {@linkplain MessageDigest#getInstance(String)}
   */
  public final String getHashAlgo() {
    return algo;
  }
  
  
  /**
   * Returns the random access index into tree structure.
   */
  public final TreeIndex<Node> idx() {
    return idx;
  }
  

  
  final boolean verify(Node node, MessageDigest digest) {
    Objects.requireNonNull(node, "node");
    Objects.requireNonNull(digest, "digest");
    if (!getHashAlgo().equals(digest.getAlgorithm()))
      throw new IllegalArgumentException(
          "Algo mismatch. Expected '" + getHashAlgo() + "'; digest's is '" + digest.getAlgorithm() + "'");
    
    if (node.isLeaf())
      return true;
    
    byte[] hash;
    try {
      byte[] left = data( node.leftChild() );
      byte[] right = data( node.rightChild() );
      
      if (node.isCarry() && node.rightChild().isLeaf())
        hash = hashUncommon(left, right, digest);
      else if (node.level() == 1)
        hash = hashLeaves(left, right, digest);
      else
        hash = hashInternals(left, right, digest);
      
    } catch (IllegalArgumentException iax) {
      return false;
    }
    
    return Arrays.equals(hash, data(node) );
  }
  
  
  final byte[] data(Node node) {
    return data(node.level(), node.index());
  }
  
  /**
   * Returns [a copy of] the data for the node at the specified coordinates.
   * For internal nodes, this is just the node's hash, which is computed from the hash
   * of its child nodes; for leaf nodes, the node's data may generally be anything, but
   * more often than not it's the hash of a source object.
   * 
   * @see Node#data()
   */
  public abstract byte[] data(int level, int index);
  
  
  /**
   * Returns the leaf width in bytes if <em>fixed</em>; -1, otherwise (variable).
   */
  public abstract int leafWidth();
  
  
  /**
   * Determines whether the width of the leaves is fixed.
   * 
   * @return <code>true</code> iff the leaf width is not variable
   */
  public final boolean isLeafWidthFixed() {
    return leafWidth() > 0;
  }
  
  /**
   * Determines whether the width of the leaves is the same as the byte
   * width of the hashing algorithm.
   */
  public final boolean isOmniWidth() {
    return hashAlgoWidth() == leafWidth();
  }
  
  
  /**
   * Returns the byte width of the hashing algorithm by observing the width
   * of the node at coordinates (1, 0), which is the first internal node constructed
   * in a tree.
   */
  public final int hashAlgoWidth() {
    return idx().getNode(1, 0).data().length;
  }
  
  
  /**
   * Returns a cryptographic path to the root of the Merkle tree.
   */
  public final Proof proof(int leafIndex) {
    return new Proof(this, leafIndex);
  }
  
  
  /**
   * For debug use.
   */
  @Override
  public String toString() {
    StringBuilder string = new StringBuilder(32);
    
    string.append(getClass().getSimpleName()).append('[');
    
    return appendToStringDetail(string).append(']').toString();
  }
  
  protected StringBuilder appendToStringDetail(StringBuilder string) {
    string.append(algo).append(':').append(idx.count());
    if (isOmniWidth())
      string.append(":omni");
    else if (isLeafWidthFixed())
      string.append(":fixed:").append(leafWidth());
    else
      string.append(":var");
    return string;
  }
  
  

  
  
  private class NodeFactory implements TreeIndex.NodeFactory<Node> {

    @Override
    public Node newNode(int level, int index, boolean right) {
      return new Node(level, index, Tree.this);
    }
    
  }

}
