DROP TABLE IF EXISTS analytic;

CREATE TABLE analytic (
   dataTime DateTime,
   model String,
   state LowCardinality(String)
) ENGINE = MergeTree()
ORDER BY (model, dataTime);
