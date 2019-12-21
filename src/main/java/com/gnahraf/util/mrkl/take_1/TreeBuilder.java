/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util.mrkl.take_1;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Objects;

/**
 * 
 */
public class TreeBuilder {
  
  private final ArrayList<Node> leaves = new ArrayList<>();
  private final ArrayList<Carry> levels = new ArrayList<>();
  private final MessageDigest digest;
  

  /**
   * Creates a new instance with a dedicated <tt>MessageDigest</tt> using the
   * given hashing algorithm.
   * 
   * @param algo the digest algorithm (e.g. MD5, SHA-1, SHA-256)
   * 
   * @throws IllegalArgumentException in lieu of checked <tt>NoSuchAlgorithmException</tt>
   */
  public TreeBuilder(String algo) throws IllegalArgumentException {
    try {
      digest = MessageDigest.getInstance(algo);
    } catch (NoSuchAlgorithmException nsax) {
      throw new IllegalArgumentException("algo: " + algo, nsax);
    }
  }
  
  
  
  public int add(byte[] data) {
    Objects.requireNonNull(data);
    return add(data, 0, data.length);
  }
  
  
  
  public synchronized int add(byte[] data, int off, int len) {
    
    Node leaf = new Node(data, off, len);
    
    Node carry = leaf;
    int size = levels.size();
    
    for (int i = 0; carry != null; ++i) {
      if (i == size) {
        levels.add(newCarry(size, carry));
        carry = null;
      } else
        carry = levels.get(i).put(carry, digest);
    }
    leaves.add(leaf);
    return leaves.size();
  }
  
  
  public synchronized Tree build() throws IllegalStateException {
    if (count() < 2)
      throw new IllegalStateException(count() + " (count) < 2");
    Carry carry = null;
    for (Carry level : levels) {
      if (level.hasCarry()) {
        carry = level;
        break;
      }
    }
    assert carry != null;
    
    Node root;
    if (carry.level == topLevel()) {
      root = carry.node;
    } else {
      Node remainder = carry.node;
      for (int index = carry.level + 1; index < levels.size(); ++index) {
        Carry head = levels.get(index);
        if (head.hasCarry())
          remainder = head.put(remainder, digest);
      }
      root = remainder;
    }
    
    Tree tree = new Tree(root, digest.getAlgorithm(), leaves);
    
    clear();
    
    return tree;
  }
  
  
  /**
   * Returns the number of data elements added.
   */
  public synchronized int count() {
    return leaves.size();
  }
  
  public synchronized void clear() {
    levels.clear();
    leaves.clear();
  }
  
  
  Carry newCarry(int level, Node node) {
    return new Carry(level, node);
  }
  
  
  private int topLevel() {
    return levels.size() - 1;
  }
  
  
  
  /**
   * Cubbyhole for the last (picture it rightmost) node at each level that has yet to be paired.
   * Here, a node's level is not measured by <em>depth</em>; it's measured by {@linkplain Node#height() height}.
   * Leaf nodes are at level <tt>0</tt>; the root is at the highest.
   */
  static class Carry {
    
    private final int level;
    private Node node;
    
    Carry(int level, Node head) {
      this.level = level;
      this.node = head;
    }
    
    final boolean hasCarry() {
      return node != null;
    }
    
    final Node put(Node next, MessageDigest digest) {
      if (this.node == null) {
        node = next;
        return null;
      
      } else {
        Node fused = fuse(next, digest);
        node = null;
        return fused;
      }
    }
    
    final Node node() {
      return node;
    }
    
    Node fuse(Node right, MessageDigest digest) {
      return new Node(node, right, digest);
    }
  }

}
