DROP TABLE IF EXISTS public.analytics_rules;

CREATE TABLE IF NOT EXISTS public.analytics_rules (
  rule_id serial PRIMARY KEY,
  service_id bigint NOT NULL,
  type text NOT NULL,
  model text,
  window_minutes int NOT NULL,
  default_value text
);
