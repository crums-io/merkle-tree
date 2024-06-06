/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.util.mrkl;


import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import io.crums.util.mrkl.index.AbstractNode;
import io.crums.util.mrkl.index.TreeIndex;
import io.crums.util.mrkl.intenal.ByteList;

/**
 * A cryptographic path from an item (expressed in bytes) to the
 * root of a Merkle tree. Note although instances are immutable, a reference to one
 * does <em>not</em> imply a {@linkplain #verify(MessageDigest) verified} proof.
 * For such a guarantee, considering defining a subclass.
 * 
 * @see #hashChain()
 * @see #verify(MessageDigest)
 */
public class Proof {
  
  private final String algo;
  
  private final int leafCount;
  private final int leafIndex;
  private final List<byte[]> hashChain;
  

  public Proof(Tree tree, int leafIndex) throws IndexOutOfBoundsException {
    this.algo = tree.getHashAlgo();
    this.leafCount = tree.idx().count();
    this.leafIndex = leafIndex;
    
    ArrayList<byte[]> chain = new ArrayList<>(tree.idx().height() + 1);
    Node node = tree.idx().getNode(0, leafIndex);
    chain.add(node.data());
    for (Node parent = node; !parent.isRoot(); parent = parent.parent())
      chain.add(parent.sibling().data());
    chain.add(tree.root().data());
    
    this.hashChain = chain;
    checkChainLength();
  }
  
  
  public Proof(String algo, int leafCount, int leafIndex, byte[][] chain) {
    this(algo, leafCount, leafIndex, chain, true);
    checkChainLength();
  }
  
  
  public Proof(String algo, int leafCount, int leafIndex, byte[][] chain, boolean copy) {
    this.algo = Objects.requireNonNull(algo, "algo");
    Objects.checkIndex(leafIndex, leafCount);
    this.leafCount = leafCount;
    this.leafIndex = leafIndex;
    this.hashChain = new ArrayList<>(chain.length);
    for (byte[] link : chain)
      hashChain.add(copy ? Arrays.copyOf(link, link.length) : link);
    checkChainLength();
  }


  private void checkChainLength() {
    int cLen = chainLength(leafCount, leafIndex);
    if (hashChain.size() != cLen)
      throw new IllegalArgumentException(
          "illegal chain length, expected " + cLen + "; but was " +
          hashChain.size());
  }
  
  
  /**
   * Copy constructor.
   */
  protected Proof(Proof copy) {
    Objects.requireNonNull(copy, "null proof");
    this.algo = copy.algo;
    this.leafCount = copy.leafCount;
    this.leafIndex = copy.leafIndex;
    this.hashChain = copy.hashChain;
  }
  
  
  
  /**
   * Verifies this proof and returns the result.
   */
  public final boolean verify(MessageDigest digest) {
    if (!digest.getAlgorithm().equals(algo))
      throw new IllegalArgumentException(
          "algo mismatch: expected '" + algo + "'; digest's '" + digest.getAlgorithm() + "'");
    
    
    byte[] rootHash = merkeRootInternal(
        leafIndex,
        leafCount,
        chain(),
        digest);
    
    return Arrays.equals(rootHash, rootHash());
  }
  
  
  
  /**
   * Returns the chain length.
   */
  public static int chainLength(int leafCount, int leafIndex) {
    
    TreeIndex<?> tree = TreeIndex.newGeneric(leafCount);
    AbstractNode node = tree.getNode(0, leafIndex);

    int count = 2;  // count self and root
    while (!tree.isRoot(node)) {
      ++count;
      // needs sibling hash to form:
      node = tree.getParent(node);
    }
    
    return count;
  }
  

  /**
   * Returns the funnel length.
   */
  public static int funnelLength(int leafCount, int leafIndex) {
    return chainLength(leafCount, leafIndex) - 2;
  }


  /**
   * Computes and returns the merkle root for the given item and proof-funnel.
   * 
   * <h4>On Proof Funnels</h4>
   * <p>
   * In many an application, a merkle proof is a link in a larger hash proof.
   * Thus, it may be possible (there's already a usecase) that both the {@code item}
   * and its merkle-coordinates (and even this method's return value) are already
   * known. In such cases, the {@code funnel} argument below is the most
   * compact scheme for establishing a cryptographic link to the root hash. 
   * </p><p>
   * This funnel concept is not unique to merkle proofs. Time permitting, I'll
   * flesh it out more.
   * <p>
   * 
   * @param item          the leaf value in the merkle tree; its coordinates follow..
   * @param index         {@code item} leaf-index in the merkle tree
   * @param count         no. of items in the merkle tree
   * @param funnel        intermediate node hashes in a merkle proof
   * @param digest        digester
   * 
   * @return  root hash of the merkle tree
   * 
   * @see #funnelLength(int, int)
   * @see #funnel()
   */
  public static byte[] merkleRoot(
      ByteBuffer item, int index, int count,
      List<ByteBuffer> funnel,
      MessageDigest digest) {

    if (funnel.size() != funnelLength(count, index))
      throw new IllegalArgumentException(
        "funnel size (" + funnel.size() + ") for " + index + ":" + count +
        "; expected " + funnelLength(count, index));

    var chain = new ArrayList<ByteBuffer>() {
          @Override public ByteBuffer get(int index) {
            return super.get(index).slice();
          }
        };

    chain.add(item);
    chain.addAll(funnel);

    return merkeRootInternal(index, count, chain, digest);
  }
  



  private static byte[] merkeRootInternal(
      int index, int count, List<ByteBuffer> hashChain,
      MessageDigest digest) {

    TreeIndex<?> tree = TreeIndex.newGeneric(count);
    AbstractNode node = tree.getSibling(0, index);

    byte[] hash;
    
    if (node.isLeaf()) {
      ByteBuffer left, right;
      if (node.isLeft()) {
        left = hashChain.get(1);
        right = hashChain.get(0);
      } else {
        left = hashChain.get(0);
        right = hashChain.get(1);
      }
      hash = Tree.hashLeaves(left, right, digest);
      
    } else {
      assert node.isLeft();
      hash = Tree.hashUncommon(hashChain.get(1), hashChain.get(0), digest);
    }
    
    node = tree.getParent(node);
    
    int cindex = 2;
    for (; node.level() != tree.height(); node = tree.getParent(node), ++cindex) {
      // invariant: *hash belongs to *node
      ByteBuffer left, right;
      AbstractNode rightNode;
      if (node.isLeft()) {
        left = ByteBuffer.wrap(hash);
        right = hashChain.get(cindex);
        rightNode = tree.getSibling(node);
        assert !node.isLeaf();
      } else {
        // node is right
        left = hashChain.get(cindex);
        right = ByteBuffer.wrap(hash);
        rightNode = node;
      }
      if (rightNode.isLeaf())
        hash = Tree.hashUncommon(left, right, digest);
      else
        hash = Tree.hashInternals(left, right, digest);
    }
    // assert cindex == funnel.size();
    return hash;
  }



  
  
  
  @Override
  public final boolean equals(Object o) {
    if (o == this)
      return true;
    else if (o instanceof Proof) {
      Proof other = (Proof) o;
      if (leafIndex != other.leafIndex || leafCount != other.leafCount || !other.algo.equals(algo))
        return false;
      return other.hashChain().equals(hashChain());
    } else
      return false;
  }
  
  
  @Override
  public final int hashCode() {
    int hash = hashChain.hashCode();
    return hash ^ leafIndex ^ (2*leafCount - 1);
  }
  
  
  /**
   * The [leaf] index of the item proven.
   */
  public final int leafIndex() {
    return leafIndex;
  }
  
  
  /**
   * Returns the total number of leaves in the tree from which this proof was constructed.
   * The number of leaves determines the structure of the tree, which in turn governs the
   * validity of the proof.
   */
  public final int leafCount() {
    return leafCount;
  }
  
  
  public final String getHashAlgo() {
    return algo;
  }
  
  
  /**
   * Returns the hash chain. The returned list is immutable (both structurally and
   * contents-wise).
   * The first element in the list is the {@linkplain #item() item}, the last element
   * is {@linkplain #rootHash() root} of the Merkle tree, and the elements in between
   * are siblings on the path to root.
   * <p>
   * So the element at index 1 is the first element's (the item's) sibling, the element
   * at index 2 is the sibling of the (implied and calculable) parent of the first 2
   * elements, the element at index 3 the sibling of the parent of the last element, and so
   * on, until the last child of root.
   * </p><p>
   * Note the returned list does not contain information on its own about the <em>handedness</em>
   * of the nodes (whether they join their siblings from the left or the right); that is
   * established the leaf index and leaf count.
   * </p>
   */
  public final List<byte[]> hashChain() {
    return new ByteList(hashChain);
  }



  /**
   * Returns the hash chain.
   * The first element in the list is [a view of] the {@linkplain #item() item}, the last element
   * is [a view of] the {@linkplain #rootHash() root} of the Merkle tree, and the elements in between
   * are siblings on the path to root.
   * <p>
   * So the element at index 1 is the first element's (the item's) sibling, the element
   * at index 2 is the sibling of the (implied and calculable) parent of the first 2
   * elements, the element at index 3 the sibling of the parent of the last element, and so
   * on, until the last child of root.
   * </p><p>
   * Note the returned list does not contain information on its own about the <em>handedness</em>
   * of the nodes (whether they join their siblings from the left or the right); that is
   * established the leaf index and leaf count.
   * </p>
   * 
   * @return list that generates a <em>new</em> buffer one each invocation of
   *          {@link List#get(int)}, of size {@code >= 3}
   */
  public final List<ByteBuffer> chain() {
    return new AbstractList<ByteBuffer>() {
          @Override public int size() { return hashChain.size(); }
          @Override public ByteBuffer get(int index) {
            return ByteBuffer.wrap(hashChain.get(index)).asReadOnlyBuffer();
          }
        };
  }


  /**
   * Returns the proof's funnel.
   * 
   * @return the {@link #chain()} with first and last elements clipped
   * 
   * @see #merkleRoot(ByteBuffer, int, int, List, MessageDigest)
   */
  public final List<ByteBuffer> funnel() {
    return chain().subList(1, hashChain.size() - 1);
  }
  
  
  /**
   * Returns[a copy of] the hash at the root of the Merkle tree.
   */
  public final byte[] rootHash() {
    byte[] root = hashChain.get(hashChain.size() - 1);
    return Arrays.copyOf(root, root.length);
  }
  
  
  /**
   * Returns [a copy of] the item proven.
   */
  public final byte[] item() {
    byte[] item = hashChain.get(0);
    return Arrays.copyOf(item, item.length);
  }

}







