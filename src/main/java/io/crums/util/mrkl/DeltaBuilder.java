/*
 * Copyright 2020 Babak Farhang
 */
package io.crums.util.mrkl;


import static io.crums.util.mrkl.intenal.Bytes.transfer;

import java.util.List;

import io.crums.util.mrkl.intenal.ListExtension;

/**
 * Builds an incrementally bigger tree by only recalculating and maintaining deltas.
 * Note this is an implementation path to building file-backed trees that would otherwise
 * have trouble fitting in memory.
 * 
 * @see DeltaTree
 */
public class DeltaBuilder extends FixedLeafBuilder {
  
  private final Tree base;
  private final int nodeWidth;
  
  private int itemsAdded;

  /**
   * Constructs a copy-on-write instance.
   * 
   * @param tree    the base tree to start from
   * 
   * @see #DeltaBuilder(Tree, boolean)
   */
  public DeltaBuilder(Tree tree) {
    this(tree, true);
  }

  
  /**
   * Creates a new instance.
   * 
   * @param tree        the base tree to start from
   * @param copyOnWrite if <code>true</code>, then every {@linkplain #add(byte[])} is argument
   *                    is copied (the argument's value is considered volatile). When you know you won't
   *                    be modifying the input arguments set this to <code>false</code>
   * 
   */
  public DeltaBuilder(Tree tree, boolean copyOnWrite) {
    super(tree.getHashAlgo(), tree.leafWidth(), copyOnWrite);
    this.base = tree;
    this.nodeWidth = base.leafWidth();
    if (!base.isOmniWidth())
      throw new IllegalArgumentException("only omni-width trees are supported: " + base);
    
    data.clear();
    
    int heightSansCarries = 31 - Integer.numberOfLeadingZeros(base.idx().count()); // =floor( log( base.idx().count() )
    for (int level = 0; level <= heightSansCarries; ++level) {
      List<byte[]> treeLevelData = new LevelByteList(base, level);
      data.add(new ListExtension<>(treeLevelData));
    }

    assert data.get(data.size() - 1).size() == 1;
    
  }
  
  
  
  @Override
  public Tree build() {
    synchronized (lock) {
      if (itemsAdded() == 0 && base.idx().totalCarries() == 0)
        return base;
      return super.build();
    }
  }
  
  

  

  @Override
  protected DeltaTree packageTree() {
    
    // we're building an AppendedTree instance.. gather the appended node data into one array
    // the data is to be arranged in serial order (breadth first)
    
    // count the number of appended nodes
    // (assume no bugs.. do some arithmetic)
    
    int newNodes = 2 * count() - 1 - base.idx().totalCountSansCarries();

    // (don't worry.. we get to check our calculation)
    
    byte[] appendedNodes = new byte[newNodes * nodeWidth];
    
    int offset = 0;
    
    for (int level = data.size(); level-- > 0; ) {
      
      List<byte[]> levelNodes = data.get(level);
      
      if (levelNodes instanceof ListExtension)
        levelNodes = ((ListExtension<byte[]>) levelNodes).second();
      
      for (byte[] node : levelNodes) {
        transfer(node, appendedNodes, offset);
        offset += nodeWidth;
      }
      
    }
    
    // sanity check
    assert offset == appendedNodes.length;
    
    return new DeltaTree(base, appendedNodes);
  }

  
  @Override
  public void clear() {
    synchronized (lock) {
      
      for (int level = data.size(); level-- > 0; ) {
        List<byte[]> levelData = data.get(level);
        if (levelData instanceof ListExtension)
          ((ListExtension<byte[]>) levelData).second().clear();
        else {
          levelData.clear();
          data.remove(level);
        }
      }
      itemsAdded = 0;
    }
  }
  
  /**
   * <p>Items only the width of the (hash) digest width are allowed because this is an
   * omni-width tree.</p>
   * 
   * {@inheritDoc}
   */
  @Override
  public int add(byte[] item, int off, int len) throws IndexOutOfBoundsException {
    synchronized (lock) {
      int indexOfItem = super.add(item, off, len);
      ++itemsAdded;
      return indexOfItem;
    }
  }
  
  
  
  /**
   * Returns the total number of items added (appended).
   */
  public int itemsAdded() {
    return itemsAdded;
  }
  

}
