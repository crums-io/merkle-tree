/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.util.mrkl.intenal;

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
    Objects.checkFromIndexSize(off, len, src.length);
    byte[] copy = new byte[len];
    for (int index = len; index-- > 0;)
      copy[index] = src[off + index];
    return copy;
  }
  
  
  public static void transfer(byte[] src, byte[] dtn, int dtnOff) {
    transfer(src, 0, dtn, dtnOff, src.length);
  }
  
  
  public static void transfer(byte[] src, int srcOff, byte[] dtn, int dtnOff, int len) {
    Objects.checkFromIndexSize(srcOff, len, src.length);
    Objects.checkFromIndexSize(dtnOff, len, dtn.length);
    while (len-- > 0)
      dtn[dtnOff++] = src[srcOff++];
  }

}
