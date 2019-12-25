/*
 * Copyright 2019 Babak Farhang
 */
package com.gnahraf.util;


import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.RandomAccess;

/**
 * A read-only, copy-on-read view of a {@code List<byte[]>}. Some caveats about
 * breaking some corners of the <tt>List</tt> contract for sake of usability.
 * 
 * @see #indexOf(Object)
 * @see #lastIndexOf(Object)
 */
public class ByteList extends AbstractList<byte[]> implements RandomAccess {
  
  protected final List<byte[]> source;

  /**
   * 
   */
  public ByteList(List<byte[]> source) {
    this.source = Objects.requireNonNull(source);
  }

  /**
   * {@inheritDoc}
   * This returns a <em>copy</em> of the source array.
   */
  @Override
  public byte[] get(int index) {
    return copy(source.get(index));
  }

  @Override
  public int size() {
    return source.size();
  }
  

  /**
   * {@inheritDoc}
   * 
   * <h4>Note</h4>
   * <p>
   * This implementation uses {@linkplain Arrays#equals(byte[], byte[])} in lieu of
   * <tt>Object.equals(o)</tt>, so in this sense, it breaks the above formal contract.
   * Strictly, the contract for {@linkplain List#equals(Object)} (and {@linkplain List#hashCode()})
   * is also violated.
   * </p>
   */
  @Override
  public int indexOf(Object o) {
    if (!(o instanceof byte[]))
      return -1;
    
    byte[] array = (byte[]) o;
    for (ListIterator<byte[]> i = source.listIterator(); i.hasNext(); )
      if (Arrays.equals(array, i.next())) // in lieu of Object.equals
        return i.previousIndex();
    
    return -1;
  }
  
  
  /**
   * {@inheritDoc}
   * 
   * <h4>Note</h4>
   * <p>
   * This implementation uses {@linkplain Arrays#equals(byte[], byte[])} in lieu of
   * <tt>Object.equals(o)</tt>, so in this sense, it breaks the above formal contract.
   * Strictly, the contract for {@linkplain List#equals(Object)} (and {@linkplain List#hashCode()})
   * is also violated.
   * </p>
   */
  @Override
  public int lastIndexOf(Object o) {
    if (!(o instanceof byte[]))
      return -1;
    
    byte[] array = (byte[]) o;
    for (ListIterator<byte[]> i = source.listIterator(source.size()); i.hasPrevious(); )
      if (Arrays.equals(array, i.previous())) // in lieu of Object.equals
        return i.nextIndex();
    
    return -1;
  }
  
  
  private byte[] copy(byte[] src) {
    byte[] copy = new byte[src.length];
    for (int b = src.length; b-- > 0; )
      copy[b] = src[b];
    return copy;
  }

}
