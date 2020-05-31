/*
 * Copyright 2020 Babak Farhang
 */
package io.crums.util.mrkl;

import static io.crums.util.mem.Bytes.transfer;

import io.crums.util.mrkl.index.TreeIndex;

/**
 * 
 */
public class FixedLeafBuilder extends Builder {
  
  private final int leafWidth;

  /**
   * @param algo
   * @throws IllegalArgumentException
   */
  public FixedLeafBuilder(String algo) {
    this(algo, true);
  }

  /**
   * @param algo
   * @throws IllegalArgumentException
   */
  public FixedLeafBuilder(String algo, int leafWidth) throws IllegalArgumentException {
    this(algo, leafWidth, true);
  }

  /**
   * @param algo
   * @param copyOnWrite
   * @throws IllegalArgumentException
   */
  public FixedLeafBuilder(String algo, int leafWidth, boolean copyOnWrite) throws IllegalArgumentException {
    super(algo, copyOnWrite);
    this.leafWidth = leafWidth;
    if (leafWidth < 1)
      throw new IllegalArgumentException("leaf width: " + leafWidth);
  }

  /**
   * @param algo
   * @param copyOnWrite
   * @throws IllegalArgumentException
   */
  public FixedLeafBuilder(String algo, boolean copyOnWrite) throws IllegalArgumentException {
    super(algo, copyOnWrite);
    this.leafWidth = hashWidth();
  }
  
  
  
  protected FixedLeafBuilder(FixedLeafBuilder copy) {
    super(copy);
    this.leafWidth = copy.leafWidth;
  }
  
  
  FixedLeafBuilder(Builder copy) {
    super(copy);
    this.leafWidth = copy.leafWidth();
    if (leafWidth < 1)
      throw new IllegalArgumentException("not a fixed leaf width: " + leafWidth);
  }
  
  

  @Override
  public int leafWidth() {
    return leafWidth;
  }

  
  /**
   * @param len   = {@linkplain #leafWidth()}
   * @throws IllegalArgumentException if <tt>len</tt> &ne; {@linkplain #leafWidth()}
   */
  @Override
  public int add(byte[] item, int off, int len) throws IllegalArgumentException, IndexOutOfBoundsException {
    if (len != leafWidth)
      throw new IllegalArgumentException("len " + len + "; expected " + leafWidth);
    
    return super.add(item, off, len);
  }
  
  
  @Override
  
  public Tree build() {
    synchronized (lock) {
      completeTree();
      Tree tree = packageTree();
      clear();
      return tree;
    }
    
  }

  
  
  @Override
  protected Tree packageTree() {
    
    int fixedByteSize = FixedLeafTree.treeDataLength(
        count(),
        digest.getDigestLength(),
        leafWidth);


    TreeIndex<?> idx = TreeIndex.newGeneric(count());
    
    byte[] buffer = new byte[fixedByteSize];
    
    int pos = 0;
    final int pWidth = digest.getDigestLength();
    
    for (int level = idx.height(); level > 0; --level) 
      for (int index = 0; index < levelSize(level); ++index, pos += pWidth)
        transfer(level(level).get(index), buffer, pos);
    
    for (int index = 0; index < count(); ++index, pos += leafWidth)
      transfer(level(0).get(index), buffer, pos);
    
    assert pos == buffer.length;
    
    return new FixedLeafTree(count(), getHashAlgo(), buffer, pWidth, leafWidth);
  }
  
  

}
