/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util.mrkl.take_1;


import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Objects;

/**
 * Bottom-up constructon of Merkle tree nodes. You don't create these directly: the
 * constructors are package-access. Instead, use the {@linkplain TreeBuilder}. This way we can
 * ensure that given an instance of this class its (immutable) state conforms to
 * it's being a member of a properly constructed merkle tree.
 * 
 * <h3>Invariants</h3>
 * <p>
 * The following conditions always hold.
 * </p>
 * <ol>
 * <li>The left branch is <tt>null</tt> <b>iff</b> the right branch is <tt>null</tt>. (If one branch is
 * present, then so is the other.)</li>
 * <li>If the left branch is a leaf, then so is the right branch. (The converse is not
 * necessarily true, since the right branch might have been "promoted".)</li>
 * <li>The height of the right branch is &le; the height of left branch. Furthermore,
 * if the height of the right branch is &lt; that of the left branch, then the following constraints
 * (enforced not here, but in {@linkplain TreeBuilder TreeBuilder}) also hold:
 *   <ol>
 *   <li><tt>this</tt> node is the rightmost node with this height.</li>
 *   <li>the right branch node is the rightmost node at its own height.</li>
 *   </ol>
 * </li>
 * </ol>
 */
public class Node {
  
  public final static byte LEAF_PAD = 0;
  public final static byte BRANCH_PAD = 1;
  
  
  
  
  private final Node left;
  private final Node right;
  
  private final byte[] hash;
  
  
  /**
   * Creates a leaf instance.
   * 
   * @param hash non-null. It can be anything: the main use case is that it's a signature
   *        of something else, but our tree doesn't really care.
   */
  Node(byte[] hash) {
    Objects.requireNonNull(hash);
    left = right = null;
    this.hash = copy(hash);
  }
  
  
  /**
   * Creates a leaf instance.
   * 
   * @param hash non-null. It can be anything: the main use case is that it's a signature
   *        of something else, but our tree doesn't really care.
   * @param off  offset into <tt>hash</tt>
   * @param len  
   */
  Node(byte[] hash, int off, int len) throws IndexOutOfBoundsException {
    Objects.requireNonNull(hash);
    left = right = null;
    this.hash = copy(hash, off, len);
  }
  
  
  /**
   * Creates an internal instance.
   * 
   * @param left
   * @param right
   * @param digest
   */
  Node(Node left, Node right, MessageDigest digest) {
    Objects.requireNonNull(left, "left");
    Objects.requireNonNull(right, "right");
    Objects.requireNonNull(digest, "digest");

    this.left = left;
    this.right = right;
    
    if (left.isInternal() && left.hashLength() != digest.getDigestLength())
      throw new IllegalArgumentException(
          "digest length mismatch: " + digest.getDigestLength() + "/" +
              left.hashLength() + " (left)");

    
    if (right.isInternal() && right.hashLength() != digest.getDigestLength())
      throw new IllegalArgumentException(
          "digest length mismatch: " + digest.getDigestLength() + "/" +
              right.hashLength() + " (right)");
    
    if (left.height() < right.height())
      throw new IllegalArgumentException(
          "left/right height mismatch: " + left.height() + "/" + right.height());
    
    this.hash = computeHash(digest);
  }
  
  
  /**
   * Copy constructor.
   */
  protected Node(Node copy) {
    this.left = copy.left;
    this.right = copy.right;
    this.hash = copy.hash;
  }
  
  /**
   * Returns a copy of the instance's hash. Note, for a leaf node, though designed for
   * a hash, this may be anything, of arbitrary length; for an {@linkplain #isInternal() internal}
   * node, it's a signature composed of the concatentation of the left and right branches.
   * 
   * @see #hash(int)
   */
  public byte[] getHash() {
    return copy(hash);
  }
  
  /**
   * Returns the hash byte at the specified <tt>index</tt>. Alternative to incurring the copy-overhead
   * of {@linkplain #getHash()}, if it matters.)
   * 
   * @throws IndexOutOfBoundsException if <tt>index</tt> is negative or &ge; {@linkplain #hashLength()}
   */
  public final byte hash(int index) throws IndexOutOfBoundsException {
    return hash[index];
  }
  
  public final int hashLength() {
    return hash.length;
  }
  
  
  /**
   * Verifies the signature of <em>this</em> instance (if it's an internal node) by recomputing
   * its hash using the given <tt>digest</tt> algo. (It doesn't drill down it's branches.)
   * 
   * @see #ensure(MessageDigest)
   */
  public final boolean verify(MessageDigest digest) {
    return verifyImpl(digest, false);
  }
  
  /**
   * Ensures the signature of <em>this</em> instance (if it's an internal node) by recomputing
   * its hash using the given <tt>digest</tt> algo. (It doesn't drill down it's branches.)
   * 
   * @throws IllegalStateException if the computed hash does not match instance's hash
   * 
   * @see #verify(MessageDigest)
   */
  public final void ensure(MessageDigest digest) throws IllegalStateException {
    verifyImpl(digest, true);
  }
  
  
  /**
   * Equaliy and hashCode semantics are governed by whether both instances are of the same type
   * (internal or leaf) and whether their {@linkplain #getHash() hash}es are the same.
   */
  @Override
  public final boolean equals(Object o) {
    if (this == o)
      return true;
    else if (o instanceof Node) {
      Node other = (Node) o;
      return (other.isLeaf() == isLeaf()) && Arrays.equals(hash, other.hash);
    } else
      return false;
  }
  
  /**
   * @see #equals(Object)
   */
  @Override
  public final int hashCode() {
    return ((int) parentPadding()) ^ Arrays.hashCode(hash);
  }
  
  
  public final boolean isLeaf() {
    return right == null;
  }
  
  /**
   * Not leaf.
   */
  public final boolean isInternal() {
    return right != null;
  }
  
  
  /**
   * Returns the left branch, or <tt>null</tt> if this {@linkplain #isLeaf() is leaf}.
   */
  public Node getLeft() {
    return left;
  }
  

  /**
   * Returns the right branch, or <tt>null</tt> if this {@linkplain #isLeaf() is leaf}.
   */
  public Node getRight() {
    return right;
  }
  
  /**
   * Returns the <em>height</em> of the node. The leaf nodes are at zero height;
   * the root node is at maximum height.
   * <p>
   * (You could compose a <tt>depth() function relative
   * to root from this; here, we're building bottom-up, and prefer state to remain
   * constant, so the "height" concept in lieu of "depth" seems to make more sense.) 
   * </p>
   */
  public int height() {
    if (isLeaf())
      return 0;
    
    
    Node nonLeafBranch;
    if (!left.isLeaf())
      nonLeafBranch = left;
    else if (!right.isLeaf())
      nonLeafBranch = right;
    else
      return 1;
    
    return nonLeafBranch.height() + 1;
  }

  /**
   * Compares this instance with another by the lexicographic ordering of their {@linkplain #getHash() hash}es.
   * 
   * @see AmbiTreeBuilder.OrderedCarry#fuse(Node, MessageDigest)
   */
  public int compareTo(Node other) {
    return Arrays.compare(this.hash, other.hash);
  }
  
  
  /**
   * Returns the instance's hash in Url-safe base 64 format (no padding).
   */
  public final String hashString() {
    return UBASE_64.encodeToString(hash);
  }
  private final static Encoder UBASE_64 = Base64.getUrlEncoder().withoutPadding();
  
  
  /**
   * Returns an abridged representation of this instance's hash enclosed in square
   * brackets.
   */
  @Override
  public String toString() {
    String hstring = hashString();
    int hlen = hstring.length();
    StringBuilder string = new StringBuilder(10);
    string.append('[');
    if (hlen <= MAX_DEBUG_HASH_LEN)
      string.append(hstring);
    else {
      string.append(hstring.substring(0, (MAX_DEBUG_HASH_LEN / 2) - 1));
      string.append('.').append('.');
      string.append(hstring.substring(hlen + 1 - (MAX_DEBUG_HASH_LEN / 2)));
    }
    return string.append(']').toString();
  }
  private final static int MAX_DEBUG_HASH_LEN = 8;
  
  private boolean verifyImpl(MessageDigest digest, boolean fail) {
    if (isLeaf())
      return true;
    if (hashLength() != digest.getDigestLength()) {
      if (fail)
        throw new IllegalStateException(
            "digest length mismatch: " + digest.getDigestLength() + "/" + hashLength());
      return false;
    }
    byte[] cHash = computeHash(digest);
    if (Arrays.equals(this.hash, cHash))
      return true;
    else if (fail)
      throw new IllegalStateException(
          this + " failed with digest algo " + digest.getAlgorithm());
    else
      return false;
  }
  
  
  byte[] computeHash(MessageDigest digest) {
    digest.reset();
    digest.update(getLeft().parentPadding());
    digest.update(getLeft().hash);
    digest.update(getRight().parentPadding());
    digest.update(getRight().hash);
    return digest.digest();
  }
  
  
  protected final byte parentPadding() {
    return isLeaf() ? LEAF_PAD : BRANCH_PAD;
  }
  
  
  
  
  
  protected final byte[] copy(byte[] array) {
    int len = array.length;
    byte[] copy = new byte[len];
    for (int i = len; i-- > 0; )
      copy[i] = array[i];
    return copy;
  }
  
  protected final byte[] copy(byte[] array, int offset, int len) {
    byte[] copy = new byte[len];
    for (int i = len; i-- > 0; )
      copy[i] = array[offset + i];
    return copy;
  }

}
