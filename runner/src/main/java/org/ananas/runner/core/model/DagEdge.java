package org.ananas.runner.core.model;

public class DagEdge {

  public String source;
  public String target;

  public DagEdge(String source, String target) {
    this.source = source;
    this.target = target;
  }

  public DagEdge() {}

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DagEdge)) {
      return false;
    }

    DagEdge dagEdge = (DagEdge) o;

    if (this.source != null ? !this.source.equals(dagEdge.source) : dagEdge.source != null) {
      return false;
    }
    return this.target != null ? this.target.equals(dagEdge.target) : dagEdge.target == null;
  }

  @Override
  public int hashCode() {
    int result = this.source != null ? this.source.hashCode() : 0;
    result = 31 * result + (this.target != null ? this.target.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "DagEdge{" + "source='" + this.source + '\'' + ", target='" + this.target + '\'' + '}';
  }
}
