--
-- Copyright (c) 2022 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

DO $$
  BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'susecredentialstype') THEN
      insert into suseCredentialsType (id, label, name)
        select sequence_nextval('suse_credtype_id_seq'), 'reportcreds', 'Reporting DB Credentials'
         where not exists(select 1 from suseCredentialsType where label = 'reportcreds');
    ELSE
      RAISE NOTICE 'suseCredentialsType does not exists';
    END IF;
  END;
$$;


CREATE TABLE IF NOT EXISTS suseMgrServerInfo
(
    server_id          NUMERIC NOT NULL
                           CONSTRAINT suse_mgr_info_sid_fk
                               REFERENCES rhnServer (id),
    mgr_evr_id         NUMERIC
                           CONSTRAINT suse_mgr_info_peid_fk
                               REFERENCES rhnPackageEVR (id),
    report_db_cred_id  NUMERIC
                           CONSTRAINT suse_mgr_info_creds_fk
                               REFERENCES suseCredentials (id)
                               ON DELETE SET NULL,
    report_db_name     VARCHAR(128),
    report_db_host     VARCHAR(256),
    report_db_port     NUMERIC DEFAULT (5432) NOT NULL,
    report_db_last_synced TIMESTAMPTZ,
    created            TIMESTAMPTZ
                           DEFAULT (current_timestamp) NOT NULL,
    modified           TIMESTAMPTZ
                           DEFAULT (current_timestamp) NOT NULL,
    CONSTRAINT suse_mgr_info_sid_uq UNIQUE (server_id)
)
;

create or replace function suse_mgr_info_mod_trig_fun() returns trigger as
$$
begin
	new.modified := current_timestamp;
	       
	return new;
end;
$$ language plpgsql;

DROP TRIGGER IF EXISTS suse_mgr_info_mod_trig ON suseMgrServerInfo;
CREATE TRIGGER suse_mgr_info_mod_trig
before insert or update on suseMgrServerInfo
for each row
execute procedure suse_mgr_info_mod_trig_fun();
