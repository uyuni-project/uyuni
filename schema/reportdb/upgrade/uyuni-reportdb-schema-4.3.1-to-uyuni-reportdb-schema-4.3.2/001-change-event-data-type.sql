DO $$
  BEGIN
    IF (SELECT data_type FROM information_schema.columns WHERE table_name = 'historyreport' AND column_name = 'event_data') != 'text' THEN
      DROP VIEW IF EXISTS HistoryReport;

      ALTER TABLE SystemAction ALTER COLUMN event_data SET DATA TYPE TEXT;

      ALTER TABLE SystemHistory ALTER COLUMN event_data SET DATA TYPE TEXT;

      CREATE OR REPLACE VIEW HistoryReport AS
            SELECT mgm_id
                      , system_id
                      , action_id AS event_id
                      , hostname
                      , event
                      , completion_time AS event_time
                      , status
                      , event_data
                      , synced_date
              FROM SystemAction

          UNION ALL

            SELECT mgm_id
                      , system_id
                      , history_id AS event_id
                      , hostname
                      , event
                      , event_time
                      , 'Done' AS status
                      , event_data
                      , synced_date
              FROM SystemHistory
      ;
    ELSE
      RAISE NOTICE 'systemaction column event_data is already of type text.';
    END IF;
  END;
$$;
