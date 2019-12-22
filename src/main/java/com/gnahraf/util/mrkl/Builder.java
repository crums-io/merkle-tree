/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util.mrkl;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.gnahraf.util.mrkl.index.AbstractNode;
import com.gnahraf.util.mrkl.index.TreeIndex;

/**
 * 
 */
public class Builder {
  
  private final List<List<byte[]>> data = new ArrayList<>();
  private final MessageDigest digest;

  /**
   * Creates a new instance with a dedicated <tt>MessageDigest</tt> using the
   * given hashing algorithm.
   * 
   * @param algo the digest algorithm (e.g. MD5, SHA-1, SHA-256)
   * 
   * @throws IllegalArgumentException in lieu of checked <tt>NoSuchAlgorithmException</tt>
   */
  public Builder(String algo) throws IllegalArgumentException {
    try {
      digest = MessageDigest.getInstance(algo);
    } catch (NoSuchAlgorithmException nsax) {
      throw new IllegalArgumentException("algo: " + algo, nsax);
    }
    data.add(new ArrayList<>());
  }
  
  
  
  /**
   * Adds the specified item as the next leaf node are returns its leaf index.
   * 
   * @param item  the item's data (copied)
   * 
   * @return      the item's leaf node index in the to-be built tree
   */
  public int add(byte[] item) {
    return add(item, 0, item.length);
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
  public synchronized int add(byte[] item, int off, int len) throws IndexOutOfBoundsException {
    
    level(0).add(copy(item, off, len));
    
    if (levelPaired(0)) {
      nextLevel(0).add(Tree.hashLeaves(lastLeft(0), lastRight(0), digest));
      
      for (int index = 1; levelPaired(index); ++index)
        nextLevel(index).add(Tree.hashInternals(lastLeft(index), lastRight(index), digest));
    }
    return count() - 1;
  }
  
  
  
  
  /**
   * Builds and returns the tree. On return the builder is cleared.
   * 
   * @see #clear()
   */
  public synchronized Tree build() {
    if (count() < 2)
      throw new IllegalStateException("nothing to build; count is " + count());
    
    // create an index and fill in the missing nodes (which we call carries)
    TreeIndex<?> idx = TreeIndex.newGeneric(count());
    
    // sanity check
    assert idx.height() == maxLevel() + 1 || idx.height() == maxLevel();
    
    for (int level = 1; level <= idx.height(); ++ level) {
      
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
    
    byte[][] bb = new byte[idx.totalCount()][];
    
    for (int serialIndex = 0, level = idx.height(); level >= 0; --level)
      for (int index = 0; index < levelSize(level); ++index, ++serialIndex)
        bb[serialIndex] = level(level).get(index);
    
    Tree tree = new Tree(bb, count(), getHashAlgo(), false);
    
    clear();
    
    return tree;
  }
  
  
  /**
   * Clears the state of the instance, as if new.
   */
  public synchronized void clear() {
    // help out the gc and clear references
    data.forEach(level -> level.clear());
    data.clear();
    data.add(new ArrayList<>());
  }
  
  
  
  public final synchronized int count() {
    return level(0).size();
  }
  
  
  
  public final String getHashAlgo() {
    return digest.getAlgorithm();
  }
  
  
  
  
  
  private byte[] copy(byte[] data, int off, int len) {
    Objects.checkFromIndexSize(off, data.length, len);
    byte[] copy = new byte[len];
    for (int index = len; index-- > 0;)
      copy[index] = data[off + index];
    return copy;
  }
  
  
  private int levelSize(int level) {
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
      level = new ArrayList<>();
      data.add(level);
    } else
      level = data.get(index);
    return level;
  }
  
  private List<byte[]> level(int index) {
    return data.get(index);
  }
  
  private List<byte[]> nextLevel(int index) {
    return ensureLevel(index + 1);
  }

}
