/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.util.mrkl;


import static io.crums.util.mrkl.intenal.Bytes.copy;

import java.util.Objects;

/**
 * A <tt>Tree</tt> allowing variable length leaves. Note although less efficient,
 * this layout allows larger in-memory fixed-width trees than {@linkplain FixedLeafTree}.
 */
public class FreeLeafTree extends Tree {

  private final byte[][] data;

  /**
   * Creates a new instance. Defensively copies.
   * 
   * @param data   double array of length 2 x <tt>leaves</tt> - 1
   * @param leaves the number of leaf nodes
   * @param algo   the hashing algorithm
   */
  public FreeLeafTree(byte[][] data, int leaves, String algo) {
    this(data, leaves, algo, true);
  }

  /**
   * Creates a new instance.
   * 
   * @param data   double array of length 2 x <tt>leaves</tt> - 1
   * @param leaves the number of leaf nodes
   * @param algo   the hashing algorithm
   * @param copy   if <tt>true</tt> a defensive copy of <tt>data</tt> is used instead;
   *               if <tt>false</tt>, then <tt>data</tt> should not be modified
   */
  public FreeLeafTree(byte[][] data, int leaves, String algo, boolean copy) {
    super(leaves, algo);
    Objects.requireNonNull(data, "data");
    
    this.data = copy ? deepCopy(data) : data;
    
    if (data.length != idx().totalCount())
      throw new IllegalArgumentException(
          "expected " + idx().totalCount() + " data elements for " +
              leaves + " many leaves; only " + data.length + " are given");
  }
  

  
  @Override
  public byte[] data(int level, int index) {
    int serialIndex = idx().serialIndex(level, index);
    return copy(data[serialIndex]);
  }
  
  
  public final byte[] data(int serialIndex) {
    return copy(data[serialIndex]);
  }

  private byte[][] deepCopy(byte[][] data) {
    byte[][] copy = new byte[data.length][];
    for (int index = data.length; index-- > 0; )
      copy[index] = copy(data[index]);
    return copy;
  }

  /**
   * <p>Returns -1 signifying <em>variable</em> width. Note, it may coincidentally happen that all the
   * leaves are fixed width. If you've specifically arranged for this, but are for some reason using
   * this class instead of {@linkplain FixedLeafTree}, then override this method to return its fixed value.
   * </p>
   * 
   * {@inheritDoc}
   * 
   * @return -1
   */
  @Override
  public int leafWidth() {
    return -1;
  }  

}
