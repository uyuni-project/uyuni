CREATE TABLE IF NOT EXISTS susePaygDimensionComputation
(
    id                   NUMERIC NOT NULL
                            CONSTRAINT susePaygDimensionComputation_pk PRIMARY KEY,
    timestamp            TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL,
    success              BOOLEAN
);

CREATE SEQUENCE IF NOT EXISTS susePaygDimensionComputation_id_seq;

CREATE INDEX IF NOT EXISTS susePaygDimensionComputation_timestamp_idx
    ON susePaygDimensionComputation (timestamp DESC);

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'billing_dimension_t') THEN
        CREATE TYPE billing_dimension_t AS ENUM (
            'managed_systems',
            'monitoring'
        );
    ELSE
        RAISE NOTICE 'type "billing_dimension_t" already exists, skipping';
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS susePaygDimensionResult
(
    id                NUMERIC NOT NULL
                          CONSTRAINT susePaygDimensionResult_id_pk PRIMARY KEY,
    computation_id    NUMERIC NOT NULL
                          CONSTRAINT susePaygDimensionResult_computation_fk
                              REFERENCES susePaygDimensionComputation (id),
    dimension         billing_dimension_t NOT NULL,
    count             NUMERIC NOT NULL
);

CREATE SEQUENCE IF NOT EXISTS susePaygDimensionResult_id_seq;
