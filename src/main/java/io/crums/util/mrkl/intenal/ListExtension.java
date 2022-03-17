/*
 * Copyright 2020 Babak Farhang
 */
package io.crums.util.mrkl.intenal;


import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;

/**
 * A first list extended by a second. The present use case is when the first list is read-only,
 * but this class imposes no restrictions. This is just a serial view of 2 otherwise independent
 * lists.
 */
public class ListExtension<T> extends AbstractList<T> implements RandomAccess {
  
  private final List<T> first;
  private final List<T> second;
  
  
  /**
   * Creqte a new instance by extending the <tt>first</tt> list with an {@linkplain ArrayList}.
   */
  public ListExtension(List<T> first) {
    this(first, new ArrayList<>());
  }

  /**
   * Creates an instance composed of the given 2 lists.
   */
  public ListExtension(List<T> first, List<T> second) {
    this.first = Objects.requireNonNull(first, "first");
    this.second = Objects.requireNonNull(second, "second");
  }
  
  
  

  /**
   * <p>Adds to the {@linkplain #second() second}.</p>
   * 
   * {@inheritDoc}
   */
  @Override
  public boolean add(T e) {
    return second.add(e);
  }



  /**
   * <p>Adds to the {@linkplain #second() second}.</p>
   * 
   * {@inheritDoc}
   */
  @Override
  public boolean addAll(Collection<? extends T> c) {
    return second.addAll(c);
  }





  /**
   * Returns the first list.
   */
  public List<T> first() {
    return first;
  }
  
  /**
   * Returns the second list.
   */
  public List<T> second() {
    return second;
  }

  
  @Override
  public T get(int index) {
    int fz = first.size();
    return index < fz ? first.get(index) : second.get(index - fz);
  }

  
  /**
   * @return the sum of the sizes of the {@linkplain #first()} and {@linkplain #second()}
   *         internal lists.
   */
  @Override
  public int size() {
    return first.size() + second.size();
  }

}
