package org.ananas.runner.misc;

import java.util.Objects;

public class MutableQuadruple<A, B, C, D> {

  private static final long serialVersionUID = 1L;

  public A first;
  public B second;
  public C third;
  public D fourth;

  public MutableQuadruple(A a, B b, C c, D d) {
    this.first = a;
    this.second = b;
    this.third = c;
    this.fourth = d;
  }

  public static <A, B, C, D> MutableQuadruple of(A a, B b, C c, D d) {
    return new MutableQuadruple<>(a, b, c, d);
  }

  public A getFirst() {
    return this.first;
  }

  public void setFirst(A first) {
    this.first = first;
  }

  // remaining getters and setters here...
  // etc...

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 23 * hash + Objects.hashCode(this.first);
    hash = 23 * hash + Objects.hashCode(this.second);
    hash = 23 * hash + Objects.hashCode(this.third);
    hash = 23 * hash + Objects.hashCode(this.fourth);
    return hash;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MutableQuadruple<A, B, C, D> other = (MutableQuadruple<A, B, C, D>) obj;
    if (!Objects.equals(this.first, other.first)) {
      return false;
    }
    if (!Objects.equals(this.second, other.second)) {
      return false;
    }
    if (!Objects.equals(this.third, other.third)) {
      return false;
    }
    if (!Objects.equals(this.fourth, other.fourth)) {
      return false;
    }
    return true;
  }
}
