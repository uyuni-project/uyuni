--
-- Copyright (c) 2021 SUSE LLC
--
--  This software is licensed to you under the GNU General Public License,
--  version 2 (GPLv2). There is NO WARRANTY for this software, express or
--  implied, including the implied warranties of MERCHANTABILITY or FITNESS
--  FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
--  along with this software; if not, see
--  http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--  Red Hat trademarks are not licensed under GPLv2. No permission is
--  granted to use or replicate Red Hat trademarks that are incorporated
--  in this software or its documentation.
--

---------------------------------
-- payg ssh connection data table
---------------------------------
CREATE TABLE IF NOT EXISTS susePaygSshData
(
    id                   NUMERIC                                 NOT NULL
        CONSTRAINT susePaygSshData_pk PRIMARY KEY,
    description          VARCHAR(255),
    host                 VARCHAR(255)                            NOT NULL,
    port                 NUMERIC,
    username             VARCHAR(32)                             NOT NULL,
    password             VARCHAR(32),
    key                  text,
    key_password         VARCHAR(32),
    bastion_host         VARCHAR(255),
    bastion_port         NUMERIC,
    bastion_username     VARCHAR(32),
    bastion_password     VARCHAR(32),
    bastion_key          text,
    bastion_key_password VARCHAR(32),
    status               CHAR(1) DEFAULT ('P')               NOT NULL
        CONSTRAINT suse_payg_ssh_data_status_ck
            CHECK (status in ('P', 'E', 'S')),
    error_message       text,
    created              TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL,
    modified             TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL
);

CREATE SEQUENCE IF NOT EXISTS susePaygSshData_id_seq;

CREATE UNIQUE INDEX IF NOT EXISTS susePaygSshData_host_uq
    ON susePaygSshData (host);

---------------------------
-- add rmt cloud host table
---------------------------
CREATE TABLE IF NOT EXISTS suseCloudRmtHost
(
    id               NUMERIC                                 NOT NULL
        CONSTRAINT suseCloudRmtHost_pk PRIMARY KEY,
    hostname         VARCHAR(255)                            NOT NULL,
    ip_address       VARCHAR(39)                             NOT NULL,
    ssl_cert         text,
    created          TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL,
    modified         TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL,
    payg_ssh_data_id NUMERIC                                 NOT NULL,
    CONSTRAINT payg_ssh_data_id_fk
        FOREIGN KEY (payg_ssh_data_id) REFERENCES susePaygSshData (id)
);

CREATE SEQUENCE IF NOT EXISTS susecloudrmthost_id_seq;

---------------------------
-- Add new credentials type
---------------------------
DO $$
  BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'susecredentialstype') THEN
      insert into suseCredentialsType (id, label, name)
        select sequence_nextval('suse_credtype_id_seq'), 'cloudrmt', 'Cloud RMT network'
        where not exists(select 1 from suseCredentialsType where label = 'cloudrmt');
    ELSE
      RAISE NOTICE 'suseCredentialsType does not exists';
    END IF;
  END;
$$;

--------------------------------
-- Update suse credentials Table
--------------------------------
alter table susecredentials add column if not exists extra_auth bytea;

-- Add link from credentials to payg ssh data
alter table susecredentials
    add column if not exists payg_ssh_data_id NUMERIC;

ALTER TABLE susecredentials
DROP CONSTRAINT IF EXISTS suse_credentials_payg_ssh_data_id_fk;

ALTER TABLE susecredentials
    ADD CONSTRAINT suse_credentials_payg_ssh_data_id_fk
        FOREIGN KEY (payg_ssh_data_id) REFERENCES susePaygSshData(id);

ALTER TABLE susecredentials
DROP CONSTRAINT IF EXISTS suse_credentials_payg_ssh_data_id_uq;

ALTER TABLE susecredentials
    ADD CONSTRAINT suse_credentials_payg_ssh_data_id_uq unique (payg_ssh_data_id);

----------------
-- add new tasks
----------------
INSERT INTO rhnTaskoTask (id, name, class)
  select sequence_nextval('rhn_tasko_task_id_seq'), 'update-payg-auth', 'com.redhat.rhn.taskomatic.task.payg.PaygUpdateAuthTask'
  WHERE NOT EXISTS (
            SELECT 1 FROM rhnTaskoTask WHERE
                name = 'update-payg-auth'
    );

INSERT INTO rhnTaskoTask (id, name, class)
  select sequence_nextval('rhn_tasko_task_id_seq'), 'update-payg-hosts', 'com.redhat.rhn.taskomatic.task.payg.PaygUpdateHostsTask'
  WHERE NOT EXISTS (
        SELECT 1 FROM rhnTaskoTask WHERE
                name = 'update-payg-hosts'
    );

INSERT INTO rhnTaskoBunch (id, name, description, org_bunch)
select sequence_nextval('rhn_tasko_bunch_id_seq'), 'update-payg-data-bunch', 'Runs update-payg-data
Parameters:
- integer parameter payg instance ID
- without parameter updates data for all instances', null
    WHERE NOT EXISTS ( SELECT 1 FROM rhnTaskoBunch WHERE name = 'update-payg-data-bunch');

INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
  select sequence_nextval('rhn_tasko_template_id_seq'),
        (SELECT id FROM rhnTaskoBunch WHERE name = 'update-payg-data-bunch'),
        (SELECT id FROM rhnTaskoTask WHERE name = 'update-payg-auth'),
        0,
        null
  WHERE NOT EXISTS
      ( SELECT 1 FROM rhnTaskoTemplate
          WHERE bunch_id = ( SELECT id FROM rhnTaskoBunch WHERE name = 'update-payg-data-bunch' )
          and task_id = (SELECT id FROM rhnTaskoTask WHERE name = 'update-payg-auth'));

INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
 select sequence_nextval('rhn_tasko_template_id_seq'),
        (SELECT id FROM rhnTaskoBunch WHERE name = 'update-payg-data-bunch'),
        (SELECT id FROM rhnTaskoTask WHERE name = 'update-payg-hosts'),
        1,
        null
  WHERE NOT EXISTS
    ( SELECT 1 FROM rhnTaskoTemplate
      WHERE bunch_id = ( SELECT id FROM rhnTaskoBunch WHERE name = 'update-payg-data-bunch' )
      and task_id = (SELECT id FROM rhnTaskoTask WHERE name = 'update-payg-hosts'));

-- need to add new schedule
INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
  select sequence_nextval('rhn_tasko_schedule_id_seq'), 'update-payg-default',
       (SELECT id FROM rhnTaskoBunch WHERE name='update-payg-data-bunch'),
       current_timestamp, '0 0/10 * * * ?'
  WHERE not exists (
        SELECT 1 FROM rhnTaskoSchedule
        WHERE job_label = 'update-payg-default'
  );
