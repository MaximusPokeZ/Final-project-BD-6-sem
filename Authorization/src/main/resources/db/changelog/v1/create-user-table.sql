CREATE TABLE IF NOT EXISTS "user" (
                                      id BIGSERIAL PRIMARY KEY,
                                      username VARCHAR NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS "filter_rules" (
                                                   id bigserial NOT NULL PRIMARY KEY,
                                                   filter_id bigint NOT NULL,
                                                   rule_id bigint NOT NULL,
                                                   field_name text NOT NULL,
                                                   filter_function_name text NOT NULL,
                                                   filter_value text NOT NULL
);


CREATE TABLE IF NOT EXISTS "deduplication_rules" (
                                                          id bigserial PRIMARY KEY ,
                                                          deduplication_id bigint NOT NULL,
                                                          rule_id bigint NOT NULL,
                                                          field_name text NOT NULL,
                                                          time_to_live_sec bigint NOT NULL,
                                                          is_active bool NOT NULL
);

CREATE TABLE IF NOT EXISTS "enrichment_rules" (
                                                       id bigserial NOT NULL PRIMARY KEY,
                                                       enrichment_id bigint NOT NULL,
                                                       rule_id bigint NOT NULL,
                                                       field_name text NOT NULL,
                                                       field_name_enrichment text NOT NULL,
                                                       field_value text NOT NULL,
                                                       field_value_default text NOT NULL
);

CREATE TABLE IF NOT EXISTS "analytics_rules" (
                                                      id bigserial PRIMARY KEY,
                                                      service_id bigint NOT NULL,
                                                      type text NOT NULL,
                                                      model text,
                                                      window_minutes int NOT NULL,
                                                      default_value text
);