DROP TABLE IF EXISTS analytic;

CREATE TABLE analytic (
   dataTime DateTime,
   model String,
   state LowCardinality(Nullable(String))
) ENGINE = MergeTree()
ORDER BY (model, dataTime);
