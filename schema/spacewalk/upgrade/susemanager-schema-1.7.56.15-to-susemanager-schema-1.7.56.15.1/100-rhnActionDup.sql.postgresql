-- oracle equivalent source sha1 4db4a365a2f3be6213cceb64430e6f22a95d0e69
--
-- Copyright (c) 2012, Novell Inc.
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--


CREATE TABLE rhnActionDup
(
    id                  NUMERIC NOT NULL
                            CONSTRAINT rhn_actiondup_id_pk PRIMARY KEY,
    action_id           NUMERIC NOT NULL
                            CONSTRAINT rhn_actiondup_aid_fk
                            REFERENCES rhnAction (id)
                            ON DELETE CASCADE,
    dry_run             CHAR(1)
                            DEFAULT ('N') NOT NULL
                            CONSTRAINT rhn_actiondup_dr_ck
                                CHECK (dry_run in ('Y','N')),
    full_update         CHAR(1)
                            DEFAULT ('Y') NOT NULL
                            CONSTRAINT rhn_actiondup_fu_ck
                                CHECK (full_update in ('Y','N')),
    created             timestamptz default (current_timestamp) not null,
    modified            timestamptz default (current_timestamp) not null
)
;

CREATE UNIQUE INDEX rhn_actiondup_aid_uq
ON rhnActionDup (action_id);

CREATE SEQUENCE rhn_actiondup_id_seq;

create or replace function rhn_actiondup_mod_trig_fun() returns trigger as
$$
begin
	new.modified := current_timestamp;
	return new;
end;
$$ language plpgsql;

create trigger
rhn_actiondup_mod_trig
before insert or update on rhnActionDup
for each row
execute procedure rhn_actiondup_mod_trig_fun();

