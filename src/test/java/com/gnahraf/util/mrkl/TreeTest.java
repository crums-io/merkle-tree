/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util.mrkl;


import static org.junit.Assert.fail;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * (Factored out test code.)
 */
abstract class TreeTest {
  
  public final static String ALGO = "SHA-256";
  
  public final String algo;
  
  protected TreeTest() {
    this(ALGO);
  }

  /**
   * 
   */
  protected TreeTest(String algo) {
    this.algo = algo;
  }
  
  
  
  
  protected MessageDigest newDigest() {
    try {
      return MessageDigest.getInstance(algo);
    } catch (NoSuchAlgorithmException nsax) {
      fail(nsax.getMessage());
      return null;
    }
  }
  

  
  protected Builder newBuilder() {
    return new Builder(algo);
  }

}
