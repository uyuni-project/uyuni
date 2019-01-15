CREATE OR REPLACE PROCEDURE create_seq_if_not_exists(
  isSeqName VARCHAR2,
  isSeqOptions VARCHAR2
) IS
  lnSeqCount NUMBER;
BEGIN
  -- try to find sequence in data dictionary
  SELECT count(1)
    INTO lnSeqCount
    FROM user_sequences
    WHERE UPPER(sequence_name) = UPPER(isSeqName);
  -- if sequence not found, create it
  IF lnSeqCount = 0 THEN
    EXECUTE IMMEDIATE 'CREATE SEQUENCE ' || UPPER(isSeqName) || ' ' || isSeqOptions;
  END IF;
END;
/
show errors

