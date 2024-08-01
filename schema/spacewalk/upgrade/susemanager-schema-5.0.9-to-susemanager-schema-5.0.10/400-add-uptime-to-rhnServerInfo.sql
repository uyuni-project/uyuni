--
-- add uptime data to rhnServerInfo
--

ALTER TABLE rhnServerInfo
    ADD COLUMN IF NOT EXISTS uptime_data text
;
