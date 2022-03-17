/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.util.mrkl.index;


import static org.junit.jupiter.api.Assertions.*;


import org.junit.jupiter.api.Test;

/**
 * 
 */
public class TreeIndexTest {
  
  @Test
  public void testEmpty() {
    try {
      newTreeIndex(0);
      fail();
    } catch (IllegalArgumentException expected) {  }
  }

  
  @Test
  public void testOne() {
    try {
      newTreeIndex(1);
      fail();
    } catch (IllegalArgumentException expected) {  }
  }
  
  
  @Test
  public void testRootHeightForCount() {
    for (int count = 2; count < 258; ++count)
      assertEquals(expectedHeightForCount(count), TreeIndex.rootHeightForCount(count));
  }
  
  
  @Test
  public void testTwo() {
    TreeIndex<?> tree = newTreeIndex(2);
    assertEquals(1, tree.height());
    assertEquals(2, tree.count());
    assertEquals(1, tree.count(1));
    assertEquals(1, tree.maxIndex(0));
    assertTrue(tree.isLeft(0, 0));
    
    AbstractNode node = tree.getSibling(0, 0);
    assertEquals(0, node.level());
    assertEquals(1, node.index());
    assertRight(node, tree);
    
    node = tree.getSibling(0, 1);
    assertEquals(0, node.level());
    assertEquals(0, node.index());
    assertLeft(node, tree);
    
    node = tree.getParent(0, 0);
    assertEquals(1, node.level());
    assertEquals(0, node.index());
    assertLeft(node, tree);
    
    assertEquals(node, tree.getParent(0, 1));
  }
  
  
  
  

  /**
   * Visualize the count in binary: excepting the leftmost, a '1' at digit 'd' denotes a
   * dangling odd (even index) node at level 'd':
   * 
   * 11
   */
  @Test
  public void testThree() {
    int count = 3;
    TreeIndex<?> tree = newTreeIndex(count);
    assertEquals(2, expectedHeightForCount(count));
    assertEquals(expectedHeightForCount(count), tree.height());
    assertEquals(count, tree.count());
    assertEquals(1, tree.count(1));
    assertEquals(1, tree.count(2));
    assertTrue(tree.isLeft(0, 0));
    assertTrue(tree.isRight(0, 1));
    assertTrue(tree.isRight(0, 2));
    
    AbstractNode node = tree.getSibling(0, 2);
    assertLeft(node, tree);
    assertEquals(1, node.level());
    assertEquals(0, node.index());
  }
  
  
  /**
   * 100
   */
  @Test
  public void testFour() {
    int count = 4;
    TreeIndex<?> tree = newTreeIndex(count);
    assertEquals(2, tree.height());
    assertEquals(count, tree.count());
    assertEquals(count / 2, tree.count(1));
    assertEquals(count / 4, tree.count(2));
    
  }
  
  
  /**
   * 101
   */
  @Test
  public void testFive() {
    int count = 5;
    TreeIndex<?> tree = newTreeIndex(count);
    assertEquals(3, tree.height());
    assertEquals(count, tree.count());
    assertEquals(count / 2, tree.count(1));
    assertEquals(count / 4, tree.count(2));
    assertEquals(1, tree.count(3));
    assertTrue(tree.isRight(0, count - 1));
    AbstractNode node = tree.getSibling(0, count - 1);
    assertLeft(node, tree);
    assertEquals(2, node.level());
    assertEquals(0, node.index());
    
    AbstractNode root = tree.getParent(node);
    assertEquals(tree.height(), root.level());
    assertEquals(0, root.index());
    
    assertEquals(root, tree.getParent(0, count - 1));
  }
  
  
  /**
   * 110
   */
  @Test
  public void testSix() {
    int count = 6;
    TreeIndex<?> tree = newTreeIndex(count);
    assertEquals(3, tree.height());
    assertEquals(count, tree.count());
    assertEquals(count / 2, tree.count(1));
    assertEquals(count / 4, tree.count(2));
    assertEquals(1, tree.count(3));
    assertTrue(tree.isRight(0, count - 1));
    AbstractNode sib = tree.getSibling(0, count - 1);
    assertSiblings(tree, 0, count - 1);
    assertLeft(sib, tree);
    assertEquals(0, sib.level());
    assertEquals(count - 2, sib.index());
    
    AbstractNode parent = tree.getParent(sib);
    assertEquals(1, parent.level());
    assertEquals(tree.maxIndex(parent.level()), parent.index());
    assertSiblings(tree, parent);
    assertEquals(parent, tree.getParent(sib));
    assertRight(parent, tree);
    
    AbstractNode root = tree.getParent(parent);
    assertEquals(tree.height(), root.level());
    assertEquals(0, root.index());
    
    AbstractNode pSib = tree.getSibling(parent);
    assertLeft(pSib, tree);
    assertEquals(tree.height() - 1, pSib.level());
    assertEquals(0, tree.maxIndex(pSib.level()));
    assertEquals(0, pSib.index());
    
    assertEquals(root, tree.getParent(pSib));
  }
  
  
  /**
   * 111
   */
  @Test
  public void testSeven() {
    int count = 7;
    TreeIndex<?> tree = newTreeIndex(count);
    assertEquals(3, tree.height());
    assertEquals(count, tree.count());
    assertEquals(count / 2, tree.count(1));
    assertTrue(tree.hasCarry(2));
    assertEquals((count / 4) + 1, tree.count(2));
    assertEquals(1, tree.count(3));
    assertTrue(tree.isRight(0, count - 1));
    
    AbstractNode sib = tree.getSibling(0, count - 1);
    assertSiblings(tree, 0, count - 1);
    assertTrue(sib.isLeft());
    assertEquals(1, sib.level());
    
    AbstractNode parent = tree.getParent(sib);
    assertSiblings(tree, parent);
    assertRight(parent, tree);
    assertTrue(tree.hasCarry(parent.level()));
    assertEquals(parent.index(), tree.maxIndex(parent.level()));
    
    AbstractNode root = tree.getParent(parent);
    assertEquals(tree.height(), root.level());
  }
  
  
  
  /**
   * 1000
   */
  @Test
  public void testEight() {
    int count = 8;
    TreeIndex<?> tree = newTreeIndex(count);
    assertEquals(3, tree.height());
    assertEquals(count, tree.count());
    assertEquals(count / 2, tree.count(1));
    assertEquals(count / 4, tree.count(2));
    assertEquals(count / 8, tree.count(3));
    assertTrue(tree.isRight(0, count - 1));
  }
  
  
  
  /**
   * 1001
   */
  @Test
  public void testNine() {
    int count = 9;
    TreeIndex<?> tree = newTreeIndex(count);
    assertEquals(4, tree.height());
    assertEquals(count, tree.count());
    assertEquals(count / 2, tree.count(1));
    assertEquals(count / 4, tree.count(2));
    assertEquals(count / 8, tree.count(3));
    assertEquals(1, tree.count(tree.height()));
    assertTrue(tree.isRight(0, count - 1));
    
    AbstractNode sib = tree.getSibling(0, count - 1);
    assertSiblings(tree, sib);
    assertTrue(sib.isLeft());
    assertEquals(3, sib.level());
    assertEquals(0, sib.index());
    
    AbstractNode root = tree.getParent(sib);
    assertRoot(tree, root);
  }
  

  /**
   * I should trust the proof.. but what about the implementation (?)
   */
  @Test
  public void testTotalCount() {
    int maxLeaves = 32 * 1024 * 1024 + 32;
    for (int leaves = 2; leaves < maxLeaves; ++leaves)
      assertTotalCount(TreeIndex.newGeneric(leaves));
    
    int end = 1 + Integer.MAX_VALUE / 2;
    int start = end - maxLeaves;
    for (int leaves = start; leaves < end; ++leaves)
      assertTotalCount(TreeIndex.newGeneric(leaves));
  }
  
  
  private void assertTotalCount(TreeIndex<?> tree) {
    int count = 0;
    for (int level = 0; level <= tree.height(); ++level)
      count += tree.count(level);
    
    assertEquals(count, tree.totalCount());
  }
  
  
  
  private int expectedHeightForCount(int count) {
    int height = 1;
    for (; (1 << height) < count; ++height);
    return height;
  }
  
  
  private void assertRight(AbstractNode node, TreeIndex<?> tree) {
    assertTrue(node.isRight());
    assertTrue(tree.isRight(node.level(), node.index()));
  }
  
  private void assertLeft(AbstractNode node, TreeIndex<?> tree) {
    assertTrue(node.isLeft());
    assertTrue(tree.isLeft(node.level(), node.index()));
  }

  /**
   * Verifies its siblings, their common parent, and level/index invariants across the 3.
   */
  void assertSiblings(TreeIndex<?> tree, AbstractNode node) {
    assertSiblings(tree, node.level(), node.index());
  }
  
  /**
   * Verifies the siblings, their common parent, and level/index invariants across the 3.
   */
  void assertSiblings(TreeIndex<?> tree, int level, int index) {
    AbstractNode sibA = tree.getSibling(level, index);
    AbstractNode sibB = tree.getSibling(sibA);
    assertEquals(level, sibB.level());
    assertEquals(index, sibB.index());
    AbstractNode left, right;
    if (sibA.isLeft()) {
      left = sibA;
      right = sibB;
    } else {
      left = sibB;
      right = sibA;
    }
    assertLeft(left, tree);
    assertRight(right, tree);
    assertTrue(left.level() >= right.level());
    
    AbstractNode parent = tree.getParent(right);
    assertEquals(left.level() + 1, parent.level());
    assertEquals(left.index() / 2, parent.index());
    assertEquals(parent, tree.getParent(left));
  }
  
  
  private void assertRoot(TreeIndex<?> tree, AbstractNode root) {
    assertEquals(tree.height(), root.level());
    assertEquals(0, root.index());
    assertEquals(1, tree.count(root.level()));
  }
  
  
  
  private TreeIndex<AbstractNode> newTreeIndex(int count) {
    return new TreeIndex<>(count, AbstractNode.FACTORY);
  }
 

}
