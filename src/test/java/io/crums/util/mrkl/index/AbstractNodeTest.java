/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.util.mrkl.index;


import static org.junit.Assert.*;

import org.junit.Test;

/**
 * 
 */
public class AbstractNodeTest {
  
  
  
  @Test
  public void testEquals() {
    AbstractNode node = newNode(3, 11);
    assertEquals(node, node);
    assertEquals(node, newNode(3, 11));
    
    AbstractNode other = newNode(3, 11, false);
    assertNotEquals(node, other);
    
    other = newNode(3,  10);
    assertNotEquals(node, other);
    
    other = newNode(2,  11);
    assertNotEquals(node, other);
  }
  
  

  @Test
  public void testHashCode() {
    AbstractNode node = newNode(3, 11);
    assertEquals(node.hashCode(), node.hashCode());
    assertEquals(node.hashCode(), newNode(3, 11).hashCode());
    
    AbstractNode other = newNode(3, 11, false);
    assertNotEquals(node.hashCode(), other.hashCode());
    
    other = newNode(3,  10);
    assertNotEquals(node.hashCode(), other.hashCode());
    
    other = newNode(2,  11);
    assertNotEquals(node.hashCode(), other.hashCode());
  }
  
  private AbstractNode newNode(int level, int index) {
    return newNode(level, index, (index & 1) == 1);
  }
  
  private AbstractNode newNode(int level, int index, boolean right) {
    return AbstractNode.FACTORY.newNode(level, index, right);
  }

}
