/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util;

import java.util.Objects;

/**
 * 
 */
public class Bytes {

  private Bytes() {  }
  
  
  
  
  public static byte[] copy(byte[] src) {
    return copy(src, 0, src.length);
  }
  
  public static byte[] copy(byte[] src, int off, int len) {
    Objects.checkFromIndexSize(off, src.length, len);
    byte[] copy = new byte[len];
    for (int index = len; index-- > 0;)
      copy[index] = src[off + index];
    return copy;
  }

}
