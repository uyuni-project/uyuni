CREATE OR REPLACE VIEW SystemInactivityReport AS
  SELECT mgm_id
            , system_id
            , profile_name AS system_name
            , organization
            , last_checkin_time
            , (synced_date - last_checkin_time) AS inactivity
            , synced_date
    FROM system
ORDER BY mgm_id, system_id, organization
;
