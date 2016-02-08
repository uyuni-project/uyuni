CREATE TABLE suseMatcherRunData (
    id             NUMBER NOT NULL
                   CONSTRAINT suse_matcher_run_data_pk PRIMARY KEY,
    inputBinary blob,
    outputBinary blob,
    subscriptionReportBinary blob,
    messageReportBinary blob,
    unmatchedSystemReportBinary blob
);

CREATE SEQUENCE suse_matcher_run_data_id_seq;

