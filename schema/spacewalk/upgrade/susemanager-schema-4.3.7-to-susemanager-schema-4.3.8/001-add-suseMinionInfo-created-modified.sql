-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

ALTER TABLE suseMinionInfo ADD COLUMN IF NOT EXISTS
    created   TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL;

ALTER TABLE suseMinionInfo ADD COLUMN IF NOT EXISTS
    modified  TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL;

CREATE OR REPLACE function suse_minion_info_mod_trig_fun() RETURNS TRIGGER AS
$$
BEGIN
        new.modified := current_timestamp;
        RETURN new;
END;
$$ language plpgsql;

DROP TRIGGER IF EXISTS suse_minion_info_mod_trig ON suseMinionInfo;

CREATE TRIGGER
suse_minion_info_mod_trig
BEFORE INSERT OR UPDATE ON suseMinionInfo
FOR EACH ROW
EXECUTE PROCEDURE suse_minion_info_mod_trig_fun();
