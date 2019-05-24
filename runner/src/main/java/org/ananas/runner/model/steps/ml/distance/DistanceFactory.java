package org.ananas.runner.model.steps.ml.distance;

import smile.math.distance.*;

/** Creates Distances */
public class DistanceFactory {

  public static Distance of(String label) {
    switch (label.toLowerCase()) {
      case "chebyshev":
        return new ChebyshevDistance();
      case "correlation":
        return new CorrelationDistance();
        // case "dynamictimewarping":
        //	return new DynamicTimeWarping();
      case "edit":
        return new EditDistance(255);
      case "euclidean":
        return new EuclideanDistance();
      case "jaccard":
        return new JaccardDistance();
      case "jensenshannon":
        return new JensenShannonDistance();
        // case "lee":
        //	return new LeeDistance();
        // case "mahalanobis":
        //	return new MahalanobisDistance();
      case "manhattan":
        return new ManhattanDistance();
        // case "minkowski":
        //	return new MinkowskiDistance();
      case "sparsechebyshev":
        return new SparseChebyshevDistance();
      case "sparseeuclidean":
        return new SparseEuclideanDistance();
      case "sparsemanhattan":
        return new SparseManhattanDistance();
        // case "sparseminkowski":
        //	return new SparseMinkowskiDistance();
        //	case "TaxonomicDistance":
        //		return new TaxonomicDistance();
      default:
        return new JaccardDistance();
    }
  }
}
