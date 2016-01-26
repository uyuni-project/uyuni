CREATE TABLE suseMatcherRunData (
    id             NUMBER NOT NULL
                   PRIMARY KEY,
    input text,
    output text,
    subscriptionReport text,
    messageReport text,
    unmatchedSystemReport text
);

CREATE SEQUENCE suse_matcher_run_data_id_seq;

