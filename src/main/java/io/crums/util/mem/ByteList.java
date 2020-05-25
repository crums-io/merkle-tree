/*
 * Copyright 2019 Babak Farhang
 */
package io.crums.util.mem;


import static io.crums.util.mem.Bytes.copy;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.RandomAccess;

/**
 * A read-only, copy-on-read view of a {@code List<byte[]>}. Some caveats about
 * breaking some corners of the <tt>List</tt> contract for sake of usability.
 * 
 * @see #equals(Object)
 * @see #contains(Object)
 * @see #indexOf(Object)
 * @see #lastIndexOf(Object)
 */
public class ByteList extends AbstractList<byte[]> implements RandomAccess {
  
  protected final List<byte[]> source;

  
  /**
   * Creates a new instance. Defensively copies.
   * 
   * @see #newInstance(byte[][], boolean)
   */
  public static ByteList newInstance(byte[][] arrays) {
    return newInstance(arrays, true);
  }
  
  /**
   * Creates a new instance.
   * 
   * @param arrays non-null source
   * @param copy   if <tt>true</tt> then the array is defensively copied; if <tt>false</tt>
   *               then modifications in the sub arrays (<tt>byte[]</tt>) are visible in the
   *               new instance
   * 
   * @return a <tt>List</tt> view of <tt>arrays</tt>.
   */
  public static ByteList newInstance(byte[][] arrays, boolean copy) {
    ArrayList<byte[]> base = new ArrayList<>(arrays.length);
    for (int index = 0; index < arrays.length; ++index)
      base.add(copy ? copy(arrays[index]) : arrays[index]);
    return new ByteList(base);
  }
  
  
  /**
   * Creates an instance from the given <tt>source</tt> instance. Modifications in the source are
   * visible in this view.
   */
  public ByteList( List<byte[]> source ) {
    this.source = Objects.requireNonNull(source);
  }
  
  // Note: NO COPY CONSTRUCTOR
  // (o.w. you can derive a subclass that can modify the contents of an *instance of this class.)
  

  /**
   * {@inheritDoc}
   * 
   * @return a <em>copy</em> of the item at the specified <tt>index</tt>
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
  public boolean contains(Object o) {
    if (!(o instanceof byte[]))
      return false;
    byte[] b = (byte[]) o;
    for (byte[] array : source)
      if (Arrays.equals(array, b))
        return true;
    return false;
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
  
  
  /**
   * {@inheritDoc}
   * 
   * Equality here is implemented using {@linkplain Arrays#equals(byte[], byte[])}.
   * An instance of this class may only equal another instance of itself.
   * 
   * <h4>Programmer's Note</h4>
   * <p>
   * Technically, per the {@linkplain List#equals(Object)} contract, <em>this</em> <tt>List</tt>
   * should not equal any other <tt>List</tt> instance but itself. This is because Java
   * array objects implement equality by reference (or rather, don't override {@linkplain Object#equals(Object)}),
   * and since this class returns a <em>new</em> byte array on each read, its elements equal no other.
   * </p><p>
   * Generally, if no other class of object thinks it's equal to instances of your class, your class is
   * free to redefine equality (the reflexitivity requirement). You usually only have one shot at it;
   * it's a rare case where <tt>Object.equals(Object)</tt> can be overridden <em>twice</em>.
   * </p>
   * 
   * @see #hashCode()
   */
  @Override
  public final boolean equals(Object o) {
    if (super.equals(o))
      return true;
    if (o == this)
      return true;
    else if (o instanceof ByteList) {
      ByteList other = (ByteList) o;
      if (size() != other.size())
        return false;
      for (int index = size(); index-- > 0; )
        if (!Arrays.equals(source.get(index), other.source.get(index)))
          return false;
      return true;
    } else  // we don't consider generic List<byte[]> types
            // because of the reflexitivity requirement
      return false;
  }
  
  
  /**
   * 
   * @return a hash code consistent with {@linkplain #equals(Object)}
   */
  @Override
  public final int hashCode() {
    int hash = 0;
    for (int index = size(); index-- > 0; )
      hash ^= Arrays.hashCode(source.get(index));
    return hash;
  }
  

}
