package org.ananas.runner.legacy.steps.db;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.auto.value.AutoValue;
import com.google.common.annotations.VisibleForTesting;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.apache.beam.sdk.annotations.Experimental;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.io.BoundedSource;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.display.DisplayData;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IO to read and write data on MongoDB.
 *
 * <p>
 *
 * <h3>Reading from MongoDB</h3>
 *
 * <p>
 *
 * <p>MongoDbIO source returns a bounded collection of String as {@code PCollection<String>}. The
 * String is the JSON form of the MongoDB Document.
 *
 * <p>
 *
 * <p>To configure the MongoDB source, you have to provide the connection URI, the database name and
 * the collection name. The following example illustrates various options for configuring the
 * source:
 *
 * <p>
 *
 * <pre>{@code
 * pipelines.apply(MongoDbIO.read()
 *   .withUri("mongodb://localhost:27017")
 *   .withDatabase("my-database")
 *   .withCollection("my-collection"))
 *   // above three are required configuration, returns PCollection<String>
 *
 *   // rest of the settings are optional
 *
 * }</pre>
 *
 * <p>
 *
 * <p>The source also accepts an optional configuration: {@code withFilter()} allows you to define a
 * JSON filter to get subset of data.
 *
 * <p>
 *
 * <h3>Writing to MongoDB</h3>
 *
 * <p>
 *
 * <p>MongoDB sink supports writing of Document (as JSON String) in a MongoDB.
 *
 * <p>
 *
 * <p>To configure a MongoDB sink, you must specify a connection {@code URI}, a {@code
 * DatabaseHelper} name, a {@code Collection} name. For instance:
 *
 * <p>
 *
 * <pre>{@code
 * pipelines
 *   .apply(...)
 *   .apply(MongoDbIO.write()
 *     .withUri("mongodb://localhost:27017")
 *     .withDatabase("my-database")
 *     .withCollection("my-collection")
 *     .withNumSplits(30))
 *
 * }</pre>
 */
@Experimental(Experimental.Kind.SOURCE_SINK)
public class MongoDbIO {

  private static final Logger LOG = LoggerFactory.getLogger(MongoDbIO.class);

  /** Read data from MongoDB. */
  public static MongoDbIO.Read read() {
    return new AutoValue_MongoDbIO_Read.Builder()
        .setKeepAlive(true)
        .setMaxConnectionIdleTime(60000)
        .setNumSplits(0)
        .build();
  }

  private MongoDbIO() {}

  /** A {@link PTransform} to read data from MongoDB. */
  @AutoValue
  public abstract static class Read extends PTransform<PBegin, PCollection<Document>> {
    private static final long serialVersionUID = -6301561340638945595L;

    @Nullable
    abstract String uri();

    abstract boolean keepAlive();

    abstract int maxConnectionIdleTime();

    @Nullable
    abstract String database();

    @Nullable
    abstract String collection();

    @Nullable
    abstract String filter();

    @Nullable
    abstract Integer limit();

    @Nullable
    abstract Integer sample();

    abstract int numSplits();

    abstract MongoDbIO.Read.Builder builder();

    @AutoValue.Builder
    abstract static class Builder {
      abstract MongoDbIO.Read.Builder setUri(String uri);

      abstract Read.Builder setKeepAlive(boolean keepAlive);

      abstract MongoDbIO.Read.Builder setMaxConnectionIdleTime(int maxConnectionIdleTime);

      abstract MongoDbIO.Read.Builder setDatabase(String database);

      abstract Read.Builder setCollection(String collection);

      abstract Read.Builder setFilter(String filter);

      abstract Read.Builder setNumSplits(int numSplits);

      abstract Read.Builder setLimit(Integer limit);

      abstract Read.Builder setSample(Integer sample);

      abstract Read build();
    }

    /**
     * Define the location of the MongoDB instances using an URI. The URI describes the hosts to be
     * used and some options.
     *
     * <p>
     *
     * <p>The format of the URI is:
     *
     * <p>
     *
     * <pre>{@code
     * mongodb://[username:password@]host1[:port1]...[,hostN[:portN]]][/[database][?options]]
     * }</pre>
     *
     * <p>
     *
     * <p>Where:
     *
     * <ul>
     *   <li>{@code mongodb://} is a required prefix to identify that this is a string in the
     *       standard connection format.
     *   <li>{@code username:password@} are optional. If given, the driver will attempt to login to
     *       a database after connecting to a database server. For some authentication mechanisms,
     *       only the username is specified and the password is not, in which case the ":" after the
     *       username is left off as well.
     *   <li>{@code host1} is the only required part of the URI. It identifies a server address to
     *       connect to.
     *   <li>{@code :portX} is optional and defaults to {@code :27017} if not provided.
     *   <li>{@code /database} is the name of the database to login to and thus is only relevant if
     *       the {@code username:password@} syntax is used. If not specified, the "admin" database
     *       will be used by default. It has to be equivalent with the database you specific with
     *       {@link org.apache.beam.sdk.io.mongodb.MongoDbIO.Read#withDatabase(String)}.
     *   <li>{@code ?options} are connection options. Note that if {@code database} is absent there
     *       is still a {@code /} required between the last {@code host} and the {@code ?}
     *       introducing the options. Options are name=value pairs and the pairs are separated by
     *       "{@code &}". The {@code KeepAlive} connection option can't be passed via the URI,
     *       instead you have to use {@link
     *       org.apache.beam.sdk.io.mongodb.MongoDbIO.Read#withKeepAlive(boolean)}. Same for the
     *       {@code MaxConnectionIdleTime} connection option via {@link
     *       org.apache.beam.sdk.io.mongodb.MongoDbIO.Read#withMaxConnectionIdleTime(int)}.
     * </ul>
     */
    MongoDbIO.Read withUri(String uri) {
      checkArgument(uri != null, "MongoDbIO.read().withUri(uri) called with null uri");
      return builder().setUri(uri).build();
    }

    /** Sets whether socket keep alive is enabled. */
    public MongoDbIO.Read withKeepAlive(boolean keepAlive) {
      return builder().setKeepAlive(keepAlive).build();
    }

    /** Sets sample number of documents selected randomly from its input. */
    public MongoDbIO.Read withSample(int num) {
      return builder().setSample(num).build();
    }

    /** Sets limit. */
    MongoDbIO.Read withLimit(Integer limit) {
      if (limit != null) {
        return builder().setLimit(limit).build();
      } else {
        return builder().build();
      }
    }

    /** Sets the maximum idle time for a pooled connection. */
    public MongoDbIO.Read withMaxConnectionIdleTime(int maxConnectionIdleTime) {
      return builder().setMaxConnectionIdleTime(maxConnectionIdleTime).build();
    }

    /** Sets the database to use. */
    MongoDbIO.Read withDatabase(String database) {
      checkArgument(database != null, "database can not be null");
      return builder().setDatabase(database).build();
    }

    /** Sets the collection to consider in the database. */
    MongoDbIO.Read withCollection(String collection) {
      checkArgument(collection != null, "collection can not be null");
      return builder().setCollection(collection).build();
    }

    /** Sets a filter on the documents in a collection. */
    MongoDbIO.Read withFilter(String filter) {
      return filter == null ? builder().build() : builder().setFilter(filter).build();
    }

    /** Sets the user defined number of splits. */
    public MongoDbIO.Read withNumSplits(int numSplits) {
      checkArgument(numSplits >= 0, "invalid num_splits: must be >= 0, but was %s", numSplits);
      return builder().setNumSplits(numSplits).build();
    }

    @Override
    public PCollection<Document> expand(PBegin input) {
      checkArgument(uri() != null, "withUri() is required");
      checkArgument(database() != null, "withDatabase() is required");
      checkArgument(collection() != null, "withCollection() is required");
      return input.apply(
          org.apache.beam.sdk.io.Read.from(new MongoDbIO.BoundedMongoDbSource(this)));
    }

    @Override
    public void populateDisplayData(DisplayData.Builder builder) {
      super.populateDisplayData(builder);
      builder.add(DisplayData.item("uri", uri()));
      builder.add(DisplayData.item("keepAlive", keepAlive()));
      builder.add(DisplayData.item("maxConnectionIdleTime", maxConnectionIdleTime()));
      builder.add(DisplayData.item("database", database()));
      builder.add(DisplayData.item("collection", collection()));
      builder.addIfNotNull(DisplayData.item("filter", filter()));
      builder.addIfNotNull(DisplayData.item("limit", limit()));
      builder.addIfNotNull(DisplayData.item("sample", sample()));
      builder.add(DisplayData.item("numSplit", numSplits()));
    }
  }

  /** A MongoDB {@link BoundedSource} reading {@link Document} from a given instance. */
  @VisibleForTesting
  static class BoundedMongoDbSource extends BoundedSource<Document> {
    private static final long serialVersionUID = 3275600521859223767L;
    private MongoDbIO.Read spec;

    private BoundedMongoDbSource(MongoDbIO.Read spec) {
      this.spec = spec;
    }

    @Override
    public Coder<Document> getOutputCoder() {
      return SerializableCoder.of(Document.class);
    }

    @Override
    public void populateDisplayData(DisplayData.Builder builder) {
      this.spec.populateDisplayData(builder);
    }

    @Override
    public BoundedReader<Document> createReader(PipelineOptions options) {
      return new MongoDbIO.BoundedMongoDbReader(this);
    }

    @Override
    public long getEstimatedSizeBytes(PipelineOptions pipelineOptions) {
      try (MongoClient mongoClient = new MongoClient(new MongoClientURI(this.spec.uri()))) {
        return getEstimatedSizeBytes(mongoClient, this.spec.database(), this.spec.collection());
      }
    }

    private long getEstimatedSizeBytes(
        MongoClient mongoClient, String database, String collection) {
      MongoDatabase mongoDatabase = mongoClient.getDatabase(database);

      // get the Mongo collStats object
      // it gives the size for the entire collection
      BasicDBObject stat = new BasicDBObject();
      stat.append("collStats", collection);
      Document stats = mongoDatabase.runCommand(stat);

      return stats.get("size", Number.class).longValue();
    }

    @Override
    public List<BoundedSource<Document>> split(
        long desiredBundleSizeBytes, PipelineOptions options) {
      try (MongoClient mongoClient = new MongoClient(new MongoClientURI(this.spec.uri()))) {
        MongoDatabase mongoDatabase = mongoClient.getDatabase(this.spec.database());

        List<Document> splitKeys;
        if (this.spec.numSplits() > 0) {
          // the user defines his desired number of splits
          // calculate the batch size
          long estimatedSizeBytes =
              getEstimatedSizeBytes(mongoClient, this.spec.database(), this.spec.collection());
          desiredBundleSizeBytes = estimatedSizeBytes / this.spec.numSplits();
        }

        // the desired batch size is small, using default chunk size of 1MB
        if (desiredBundleSizeBytes < 1024L * 1024L) {
          desiredBundleSizeBytes = 1L * 1024L * 1024L;
        }

        // now we have the batch size (provided by user or provided by the runner)
        // we use Mongo splitVector command to get the split keys
        BasicDBObject splitVectorCommand = new BasicDBObject();
        splitVectorCommand.append(
            "splitVector", this.spec.database() + "." + this.spec.collection());
        splitVectorCommand.append("keyPattern", new BasicDBObject().append("_id", 1));
        splitVectorCommand.append("force", false);
        // maxChunkSize is the Mongo partition size in MB
        LOG.debug("Splitting in chunk of {} MB", desiredBundleSizeBytes / 1024 / 1024);
        splitVectorCommand.append("maxChunkSize", desiredBundleSizeBytes / 1024 / 1024);
        Document splitVectorCommandResult = mongoDatabase.runCommand(splitVectorCommand);
        splitKeys = (List<Document>) splitVectorCommandResult.get("splitKeys");

        List<BoundedSource<Document>> sources = new ArrayList<>();
        if (splitKeys.size() < 1) {
          LOG.debug("Split keys is low, using an unique source");
          sources.add(this);
          return sources;
        }

        LOG.debug("Number of splits is {}", splitKeys.size());
        for (String shardFilter : splitKeysToFilters(splitKeys, this.spec.filter())) {
          sources.add(new MongoDbIO.BoundedMongoDbSource(this.spec.withFilter(shardFilter)));
        }

        return sources;
      }
    }

    /**
     * Transform a list of split keys as a list of filters containing corresponding range.
     *
     * <p>
     *
     * <p>The list of split keys contains BSon Document basically containing for example:
     *
     * <ul>
     *   <li>_id: 56
     *   <li>_id: 109
     *   <li>_id: 256
     * </ul>
     *
     * <p>
     *
     * <p>This method will generate a list of range filters performing the following splits:
     *
     * <ul>
     *   <li>from the beginning of the collection up to _id 56, so basically data with _id lower
     *       than 56
     *   <li>from _id 57 up to _id 109
     *   <li>from _id 110 up to _id 256
     *   <li>from _id 257 up to the end of the collection, so basically data with _id greater than
     *       257
     * </ul>
     *
     * @param splitKeys The list of split keys.
     * @param additionalFilter A custom (user) additional filter to append to the range filters.
     * @return A list of filters containing the ranges.
     */
    @VisibleForTesting
    static List<String> splitKeysToFilters(List<Document> splitKeys, String additionalFilter) {
      ArrayList<String> filters = new ArrayList<>();
      String lowestBound = null; // lower boundary (previous split in the iteration)
      for (int i = 0; i < splitKeys.size(); i++) {
        String splitKey = splitKeys.get(i).get("_id").toString();
        String rangeFilter;
        if (i == 0) {
          // this is the first split in the list, the filter defines
          // the range from the beginning up to this split
          rangeFilter = String.format("{ $and: [ {\"_id\":{$lte:ObjectId(\"%s\")}}", splitKey);
          filters.add(formatFilter(rangeFilter, additionalFilter));
        } else if (i == splitKeys.size() - 1) {
          // this is the last split in the list, the filters define
          // the range from the previous split to the current split and also
          // the current split to the end
          rangeFilter =
              String.format(
                  "{ $and: [ {\"_id\":{$gt:ObjectId(\"%s\")," + "$lte:ObjectId(\"%s\")}}",
                  lowestBound, splitKey);
          filters.add(formatFilter(rangeFilter, additionalFilter));
          rangeFilter = String.format("{ $and: [ {\"_id\":{$gt:ObjectId(\"%s\")}}", splitKey);
          filters.add(formatFilter(rangeFilter, additionalFilter));
        } else {
          // we are between two splits
          rangeFilter =
              String.format(
                  "{ $and: [ {\"_id\":{$gt:ObjectId(\"%s\")," + "$lte:ObjectId(\"%s\")}}",
                  lowestBound, splitKey);
          filters.add(formatFilter(rangeFilter, additionalFilter));
        }

        lowestBound = splitKey;
      }
      return filters;
    }

    /**
     * Cleanly format range filter, optionally adding the users filter if specified.
     *
     * @param filter The range filter.
     * @param additionalFilter The users filter. Null if unspecified.
     * @return The cleanly formatted range filter.
     */
    private static String formatFilter(String filter, @Nullable String additionalFilter) {
      if (additionalFilter != null && !additionalFilter.isEmpty()) {
        // user provided a filter, we append the user filter to the range filter
        return String.format("%s,%s ]}", filter, additionalFilter);
      } else {
        // user didn't provide a filter, just cleanly close the range filter
        return String.format("%s ]}", filter);
      }
    }
  }

  private static class BoundedMongoDbReader extends BoundedSource.BoundedReader<Document> {
    private final BoundedMongoDbSource source;

    private MongoClient client;
    private MongoCursor<Document> cursor;
    private Document current;

    BoundedMongoDbReader(MongoDbIO.BoundedMongoDbSource source) {
      this.source = source;
    }

    @Override
    public boolean start() {
      MongoDbIO.Read spec = this.source.spec;
      MongoClientOptions.Builder optionsBuilder = new MongoClientOptions.Builder();
      optionsBuilder.maxConnectionIdleTime(spec.maxConnectionIdleTime());
      optionsBuilder.socketKeepAlive(spec.keepAlive());
      this.client = new MongoClient(new MongoClientURI(spec.uri(), optionsBuilder));

      MongoDatabase mongoDatabase = this.client.getDatabase(spec.database());

      MongoCollection<Document> mongoCollection = mongoDatabase.getCollection(spec.collection());

      if (spec.filter() == null) {
        if (spec.limit() != null) {
          this.cursor = mongoCollection.find().limit(spec.limit()).iterator();
        } else {
          this.cursor = mongoCollection.find().iterator();
        }
      } else {
        Document bson = Document.parse(spec.filter());
        if (spec.limit() != null) {
          this.cursor = mongoCollection.find(bson).limit(spec.limit()).iterator();
        } else {
          this.cursor = mongoCollection.find(bson).iterator();
        }
      }

      return advance();
    }

    @Override
    public boolean advance() {
      if (this.cursor.hasNext()) {
        this.current = this.cursor.next();
        return true;
      } else {
        return false;
      }
    }

    @Override
    public MongoDbIO.BoundedMongoDbSource getCurrentSource() {
      return this.source;
    }

    @Override
    public Document getCurrent() {
      return this.current;
    }

    @Override
    public void close() {
      try {
        if (this.cursor != null) {
          this.cursor.close();
        }
      } catch (Exception e) {
        LOG.warn("Error closing MongoDB cursor", e);
      }
      try {
        this.client.close();
      } catch (Exception e) {
        LOG.warn("Error closing MongoDB client", e);
      }
    }
  }
}
