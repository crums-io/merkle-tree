/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util.mrkl;


import static com.gnahraf.util.Bytes.copy;
/**
 * 
 */
public class FreeLeafTree extends Tree {

  private final byte[][] data;

  /**
   * @param data
   * @param leaves
   * @param algo
   */
  public FreeLeafTree(byte[][] data, int leaves, String algo) {
    this(data, leaves, algo, true);
    // TODO Auto-generated constructor stub
  }

  /**
   * @param data
   * @param leaves
   * @param algo
   * @param copy
   */
  FreeLeafTree(byte[][] data, int leaves, String algo, boolean copy) {
    super(data, leaves, algo, copy);
    this.data = copy ? deepCopy(data) : data;
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

}
