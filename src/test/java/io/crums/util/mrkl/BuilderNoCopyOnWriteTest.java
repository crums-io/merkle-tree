/*
 * Copyright 2020 Babak Farhang
 */
package io.crums.util.mrkl;

import io.crums.util.mrkl.Builder;

/**
 * 
 */
public class BuilderNoCopyOnWriteTest extends BuilderTest {

  @Override
  protected Builder newBuilder() {
    return new Builder(algo, false);
  }
  
  

}
