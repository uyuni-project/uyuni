
DO $$
  declare
    result_msg_data_type text;
  BEGIN
    SELECT data_type INTO result_msg_data_type
    FROM information_schema.columns
    WHERE table_name = 'rhnserveraction' and column_name = 'result_msg';

    IF (result_msg_data_type='character varying') THEN
      ALTER TABLE rhnserveraction ALTER COLUMN result_msg TYPE text;
    END IF;
  END;
$$;
