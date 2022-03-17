/*
 * Copyright 2020 Babak Farhang
 */
package io.crums.util.mrkl;


import static io.crums.util.mrkl.intenal.Bytes.copy;

import java.util.Objects;

/**
 * A Merkle tree constructed from another with more leaves appended. The "delta" in
 * the name refers not to deletions, but to the fact the data added and computed is
 * incremental even when the base tree is arbitrarily large.
 * 
 *  @see DeltaBuilder
 */
public class DeltaTree extends Tree {
  
  private final Tree base;
  private final byte[] deltaNodes;

  
  /**
   * Creates a new instance.
   * 
   * @param base       the base tree 
   * @param deltaNodes new nodes of the tree. (Careful you don't create garbage.)
   * 
   * @see DeltaTree#computeNewLeafCount(Tree, byte[])
   * @see DeltaBuilder
   */
  public DeltaTree(Tree base, byte[] deltaNodes) {
    super(computeNewLeafCount(base, deltaNodes), base.getHashAlgo());
    this.base = base;
    this.deltaNodes = deltaNodes;
  }
  
  
  /**
   * Copy constructor.
   */
  protected DeltaTree(DeltaTree copy) {
    super(copy);
    this.base = copy.base;
    this.deltaNodes = copy.deltaNodes;
  }
  
  
  /**
   * Infers and returns the number of leaves in the new tree given an array of deltas (additions)
   * from a base tree.
   * 
   * @param base        an omni-width base tree (expected to be large)
   * @param deltaNodes  data array of length that is a multiple of {@linkplain Tree#hashAlgoWidth()}
   * @return the new number of leaves
   */
  public static int computeNewLeafCount(Tree base, byte[] deltaNodes) throws IllegalArgumentException {
    
    Objects.requireNonNull(base, "base");
    Objects.requireNonNull(deltaNodes, "deltaNodes");
    
    if (!base.isOmniWidth())
      throw new IllegalArgumentException("only omni-width trees are supported: " + base);
    
    int nodeWidth = base.leafWidth();
    
    int newNodes = deltaNodes.length / nodeWidth;
    
    if (newNodes <= 0)
      throw new IllegalArgumentException("empty deltaNodes array");
    
    if (newNodes * nodeWidth != deltaNodes.length)
      
      throw new IllegalArgumentException(
          "deltaNodes.length " + deltaNodes.length + " not a multiple of node width " + nodeWidth);
    
    int newTotalCount = base.idx().totalCountSansCarries() + newNodes;
    
    // the total number of nodes in a Merkle tree is alway odd. Verify it..
    //
    if ((newTotalCount & 1) == 0)
      throw new IllegalArgumentException(
          "total node count after append must be odd: " + base + "; new nodes " + newNodes);
    
    return (newTotalCount + 1) / 2;
  }
  
  

  
  @Override
  public byte[] data(int level, int index) {
    if (index < base.idx().countSansCarry(level))
      return base.data(level, index);
    
    int deltaIndex = 0;
    for (int lev = idx().height(); lev > level ; --lev)
      deltaIndex += idx().count(lev) - base.idx().countSansCarry(lev);
    
    deltaIndex += (index - base.idx().countSansCarry(level));
    int offset = deltaIndex * leafWidth();
    return copy(deltaNodes, offset, base.leafWidth());
  }

  @Override
  public int leafWidth() {
    return base.leafWidth();
  }

}
