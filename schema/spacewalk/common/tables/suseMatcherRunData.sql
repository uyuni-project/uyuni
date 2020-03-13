CREATE TABLE suseMatcherRunData (
    id             NUMERIC NOT NULL
                   CONSTRAINT suse_matcher_run_data_pk PRIMARY KEY,
    inputBinary BYTEA,
    outputBinary BYTEA,
    subscriptionReportBinary BYTEA,
    messageReportBinary BYTEA,
    unmatchedProductReportBinary BYTEA
);

CREATE SEQUENCE suse_matcher_run_data_id_seq;

