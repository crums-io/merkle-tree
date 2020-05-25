/*
 * Copyright 2020 Babak Farhang
 */
package com.gnahraf.util.mrkl;


import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Test;

/**
 * 
 */
public class DeltaBuilderTest extends TreeTest {

  
  @Test
  public void test00NothingAdded() {
    Tree base = randomOmniTree(2);
    DeltaBuilder builder = new DeltaBuilder(base);
    assertEquals(2, builder.count());
    try {
      builder.build();
      fail();
    } catch (IllegalStateException expected) {  }
  }
  
  
  @Test
  public void test01Minimal() {
    int baseCount = 2;
    int addCount = 1;
    testImpl(baseCount, addCount);
  }
  
  
  void testImpl(int baseCount, int addCount) {
    Tree base = randomOmniTree(baseCount);
    DeltaBuilder builder = new DeltaBuilder(base);
    addRandom(builder, addCount);
    assertEquals(addCount + baseCount, builder.count());
    Tree tree = builder.build();
    assertHashRecurse(tree.root(), newDigest());
  }
  
  
  void addRandom(DeltaBuilder builder, int count) {
    Random random = new Random(builder.count());
    byte[] itemBuffer = new byte[builder.hashWidth()];
    
    for (int countDown = count; countDown-- > 0;) {
      random.nextBytes(itemBuffer);
      builder.add(itemBuffer);
    }
    
  }
  
  Tree randomOmniTree(int count) {
    Builder builder = newBuilder();
    Random random = new Random(count);
    byte[] itemBuffer = new byte[builder.hashWidth()];
    
    for (int countDown = count; countDown-- > 0;) {
      random.nextBytes(itemBuffer);
      builder.add(itemBuffer);
    }
      
    return builder.build();
  }
  

  
  
  @Test
  public void test02Base2Add2() {
    int baseCount = 2;
    int addCount = 2;
    testImpl(baseCount, addCount);
  }
  
  @Test
  public void test03Base2_33Add1_33() {
    for (int baseCount = 2; baseCount <= 33; ++baseCount)
      for (int addCount = 1; addCount <= 33; ++addCount)
        testImpl(baseCount, addCount);
  }

}
