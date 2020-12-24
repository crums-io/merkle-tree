/*
 * Copyright 2020 Babak Farhang
 */
package io.crums.util.mrkl;


/**
 * 
 */
public class BuilderNoCopyOnWriteTest extends BuilderTest {

  @Override
  protected Builder newBuilder() {
    return new Builder(algo, false);
  }
  
  

}
