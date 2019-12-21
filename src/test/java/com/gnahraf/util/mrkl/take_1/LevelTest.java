/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util.mrkl.take_1;


import static com.gnahraf.util.mrkl.take_1.TreeBuilderTest.*;
import static org.junit.Assert.*;

import java.security.MessageDigest;
import java.util.List;
import java.util.Random;

import org.junit.Test;

import com.gnahraf.util.mrkl.take_1.Level;
import com.gnahraf.util.mrkl.take_1.Tree;
import com.gnahraf.util.mrkl.take_1.TreeBuilder;

/**
 * 
 */
public class LevelTest {
  
  
  @Test
  public void test00Minimal() {
    Tree tree = newRandomTree(2, 16);
    Level leaves = Level.getLeaves(tree);
    assertEquals(2, leaves.nodes().size());
//    for (int i = 0; i < leaves.nodes().size(); ++i)
//      assertPathToRoot(leaves, i, tree.getRoot());
  }
  

//  void assertPathToRoot(Level level, int index, Node root) {
//    List<Node> path = level.pathToRoot(index);
//    assertEquals(level.nodes().get(index), path.get(0));
//    MessageDigest digest = newDigest();
//    for (int i = 1; i < path.size(); ++i) {
//      Node child = path.get(i - 1);
//      Node parent = path.get(i);
//      assertTrue(parent.getLeft().equals(child) || parent.getRight().equals(child));
//      parent.ensure(digest);
//    }
//    assertEquals(root, path.get(path.size() - 1));
//  }
  
  
  public static Tree newRandomTree(int count, int dataLen) {
    return newRandomTree(count, dataLen, dataLen);
  }
  
  /**
   * Returns a randomly generated tree. Careful you don't run out of memory ;)
   * 
   * @param count  &gt; 1
   * @param minDataLen &gt; 0
   * @param maxDataLen &ge; <tt>minDataLen</tt>
   */
  public static Tree newRandomTree(int count, int minDataLen, int maxDataLen) {
    
    if (count < 2)
      throw new IllegalArgumentException("count (" + count + ") < 2");
    if (minDataLen < 1)
      throw new IllegalArgumentException("minDataLen (" + minDataLen + ") < 1");
    if (maxDataLen < minDataLen)
      throw new IllegalArgumentException(
          "maxDatalen (" + maxDataLen + ") < minDataLen (" + minDataLen + ")");
    
    int fixedLen = minDataLen == maxDataLen ? maxDataLen : 0;
    int range = 1 + maxDataLen - minDataLen;  // (for Random, max val is exclusive)
    TreeBuilder builder = new TreeBuilder(ALGO);
    Random rand = new Random(count);
    for (int i = count; i-- > 0; ) {
      byte[] data = new byte[fixedLen == 0 ?  minDataLen + rand.nextInt(range) : fixedLen];
      rand.nextBytes(data);
      builder.add(data);
    }
    
    
    return builder.build();
  }

}
