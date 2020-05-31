/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.util.mrkl;


import static io.crums.util.mem.Bytes.copy;
import static io.crums.util.mem.Bytes.transfer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import io.crums.util.mrkl.index.AbstractNode;
import io.crums.util.mrkl.index.TreeIndex;

/**
 * Collects items (byte arrays) and builds a Merkle tree. If all the items (the leaves of the tree)
 * {@linkplain #add(byte[]) added} are fixed-width (and the tree's data fits under 1GB memory), then the instance
 * builds a {@linkplain FixedLeafTree}; otherwise, it builds a {@linkplain FreeLeafTree} instance.
 */
public class Builder {
  
  /**
   * Synchronization lock.
   */
  protected final Object lock;
  
  /**
   * Breadth-first view of the nodes' data.
   */
  protected final List<List<byte[]>> data;
  protected final MessageDigest digest;
  protected final boolean copyOnWrite;
  
  /**
   * Keeps track of the leaf widths seen. -2 means unset; -1 means multiple widths seen.
   */
  private int leafWidth = LEAFWIDTH_UNSET;
  
  private final static int LEAFWIDTH_UNSET = -2;
  private final static int LEAFWIDTH_VARIABLE = -1;
  

  /**
   * Creates a new copy-on-write (copy-on-add) instance with a dedicated <tt>MessageDigest</tt> using the
   * given hashing algorithm.
   * 
   * @param algo the digest algorithm (e.g. MD5, SHA-1, SHA-256)
   * 
   * @throws IllegalArgumentException in lieu of checked <tt>NoSuchAlgorithmException</tt>
   */
  public Builder(String algo) throws IllegalArgumentException {
    this(algo, true);
  }
  

  /**
   * Creates a new instance with a dedicated <tt>MessageDigest</tt> using the
   * given hashing algorithm.
   * 
   * @param algo the digest algorithm (e.g. MD5, SHA-1, SHA-256)
   * @param copyOnWrite if <tt>true</tt>, then every {@linkplain #add(byte[])} is argument
   *                    is copied (the argument's value is considered volatile). When you know you won't
   *                    be modifying the input arguments set this to <tt>false</tt>
   * 
   * @throws IllegalArgumentException in lieu of checked <tt>NoSuchAlgorithmException</tt>
   */
  public Builder(String algo, boolean copyOnWrite) throws IllegalArgumentException {
    this.lock = new Object();
    this.data = new ArrayList<>();
    try {
      digest = MessageDigest.getInstance(algo);
    } catch (NoSuchAlgorithmException nsax) {
      throw new IllegalArgumentException("algo: " + algo, nsax);
    }
    if (digest.getDigestLength() == 0)
      throw new IllegalArgumentException(algo + " implementation does not advertise hash length");
    this.copyOnWrite = copyOnWrite;
    data.add(new ArrayList<>());
  }
  
  
  /**
   * Copy constructor. The new instance shares the same fields as this instance.
   * 
   * @param copy
   */
  protected Builder(Builder copy) {
    this.lock = copy.lock;
    this.digest = copy.digest;
    this.copyOnWrite = copy.copyOnWrite;
    this.data = copy.data;
  }
  
  
  
  /**
   * Adds the specified item as the next leaf node are returns its leaf index.
   * 
   * @param item  the item's data (copied)
   * 
   * @return      the item's leaf node index in the to-be built tree
   * 
   * @see #add(byte[], int, int)
   */
  public int add(byte[] item) {
    return add(item, 0, item.length);
  }
  
  
  /**
   * Convenience method to hash using this instance's digest {@linkplain #getHashAlgo() algo}.
   * Does not affect the state of the builder.
   * 
   * @param data to be hashed
   * @return a new array containing the hash
   */
  public byte[] hash(byte[] data) {
    synchronized (lock) {
      digest.reset();
      return digest.digest(data);
    }
  }
  
  
  public final int hashWidth() {
    return digest.getDigestLength();
  }
  
  
  /**
   * Adds the specified item as the next leaf node are returns its leaf index.
   *  
   * @param item  the item's data (copied)
   * @param off   starting offset into <tt>item</tt>
   * @param len   the number of bytes following <tt>off</tt>
   * 
   * @return      the item's leaf node index in the to-be built tree
   */
  public int add(byte[] item, int off, int len) throws IndexOutOfBoundsException {
    
    synchronized (lock) {
      level(0).add(copyImpl(item, off, len));
      
      if (len != leafWidth && leafWidth != LEAFWIDTH_VARIABLE)
        leafWidth = (leafWidth == LEAFWIDTH_UNSET) ? len : LEAFWIDTH_VARIABLE;
      
      if (levelPaired(0)) {
        nextLevel(0).add(Tree.hashLeaves(lastLeft(0), lastRight(0), digest));
        
        for (int index = 1; levelPaired(index); ++index)
          nextLevel(index).add(Tree.hashInternals(lastLeft(index), lastRight(index), digest));
      }
      return count() - 1;
    }
  }
  
  
  private byte[] copyImpl(byte[] item, int off, int len) {
    if (copyOnWrite || off != 0 || len != item.length)
      return copy(item, off, len);
    else
      return item;
  }
  
  
  /**
   * Builds and returns the tree. On return the builder is cleared.
   * 
   * @see #clear()
   */
  public Tree build() {
    
    synchronized (lock) {
      
      Tree tree;
      
      if (leafWidth > 0)
        tree = new FixedLeafBuilder(this).build();
      
      else {
        completeTree();
        tree = packageTree();
      }

      clear();
      
      return tree;
    }
  }
  
  
  protected void completeTree() {
    
    if (count() < 2)
      throw new IllegalStateException("nothing to build; count is " + count());
    
    // create an index and fill in the missing nodes (which we call carries)
    TreeIndex<?> idx = TreeIndex.newGeneric(count());
    
    // sanity check
    assert idx.height() == maxLevel() + 1 || idx.height() == maxLevel();
    
    for (int level = 1; level <= idx.height(); ++level) {
      
      if (idx.hasCarry(level)) {
        
        int index = idx.maxIndex(level);
        byte[] left = nodeData(idx.getLeftChild(level, index));
        
        AbstractNode rightChild = idx.getRightChild(level, index);
        
        byte[] right = nodeData(rightChild);
        
        // compute the parent's hash
        // Note, the left child of a carry is *never a leaf
        byte[] parent;
        if (rightChild.isLeaf())
          parent = Tree.hashUncommon(left, right, digest);
        else
          parent = Tree.hashInternals(left, right, digest);
        
        ensureLevel(level).add(parent);
      }
      
      assert levelSize(level) == idx.count(level);
    }
  }
  
  
  
  protected Tree packageTree() {
    
//    Tree tree;
//    
    assert leafWidth != LEAFWIDTH_UNSET;
    
//    
//    int fixedByteSize =
//        leafWidth == LEAFWIDTH_VARIABLE ?
//            -1 :
//              FixedLeafTree.treeDataLength(
//                  count(),
//                  digest.getDigestLength(),
//                  leafWidth);

    TreeIndex<?> idx = TreeIndex.newGeneric(count());
    
    byte[][] bb = new byte[idx.totalCount()][];
    
    for (int serialIndex = 0, level = idx.height(); level >= 0; --level)
      for (int index = 0; index < levelSize(level); ++index, ++serialIndex)
        bb[serialIndex] = level(level).get(index); 
    
    
    return new FreeLeafTree(bb, count(), getHashAlgo(), false);
    
//    if (fixedByteSize <= 0) {
//      
//      byte[][] bb = new byte[idx.totalCount()][];
//      
//      for (int serialIndex = 0, level = idx.height(); level >= 0; --level)
//        for (int index = 0; index < levelSize(level); ++index, ++serialIndex)
//          bb[serialIndex] = level(level).get(index); 
//      
//      
//      tree = new FreeLeafTree(bb, count(), getHashAlgo(), false);
//      
//    } else {
//      
//      byte[] b = new byte[fixedByteSize];
//      
//      int pos = 0;
//      int pWidth = digest.getDigestLength();
//      
//      for (int level = idx.height(); level > 0; --level) 
//        for (int index = 0; index < levelSize(level); ++index, pos += pWidth)
//          transfer(level(level).get(index), b, pos);
//      
//      for (int index = 0; index < count(); ++index, pos += leafWidth)
//        transfer(level(0).get(index), b, pos);
//      
//      assert pos == b.length;
//      
//      tree = new FixedLeafTree(count(), getHashAlgo(), b, pWidth, leafWidth);
//    }
//    
//    return tree;
  }
  
  
  public int leafWidth() {
    return leafWidth;
  }
  
  
  /**
   * Clears the state of the instance, as if new.
   */
  public void clear() {
    synchronized (lock) {
      // help out the gc and clear references
      data.forEach(level -> level.clear());
      data.clear();
      data.add(new ArrayList<>());
      leafWidth = LEAFWIDTH_UNSET;
    }
  }
  
  
  
  public final int count() {
    synchronized (lock) {
      return level(0).size();
    }
  }
  
  
  
  public final String getHashAlgo() {
    return digest.getAlgorithm();
  }
  
  
  
  
  
  
  protected final int levelSize(int level) {
    return data.get(level).size();
  }
  
  private int maxLevel() {
    return data.size() - 1;
  }

  private byte[] nodeData(AbstractNode node) {
    return level(node.level()).get(node.index());
  }
  
  private byte[] lastLeft(int level) {
    return lastSib(level, 2);
  }
  
  private byte[] lastRight(int level) {
    return lastSib(level, 1);
  }
  
  private byte[] lastSib(int level, int distance) {
    assert levelPaired(level);
    List<byte[]> levelData = level(level);
    return levelData.get(levelData.size() - distance);
  }
  
  private boolean levelPaired(int index) {
    return (level(index).size() & 1) == 0;
  }
  
  private List<byte[]> ensureLevel(int index) {
    List<byte[]> level;
    if (data.size() == index) {
      level = newByteArrayList(index);
      data.add(level);
    } else
      level = data.get(index);
    return level;
  }
  
  protected List<byte[]> newByteArrayList(int level) {
    return new ArrayList<>();
  }
  
  protected final List<byte[]> level(int index) {
    return data.get(index);
  }
  
  private List<byte[]> nextLevel(int index) {
    return ensureLevel(index + 1);
  }

}
