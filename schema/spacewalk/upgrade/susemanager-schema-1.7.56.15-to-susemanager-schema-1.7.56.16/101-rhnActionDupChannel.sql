-- oracle equivalent source sha1 ee8fdd2361d2ed71ddc577802910ffea44fef010
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


CREATE TABLE rhnActionDupChannel
(
    action_dup_id       NUMERIC NOT NULL
                            CONSTRAINT rhn_actdupchan_dupid_fk
                            REFERENCES rhnActionDup (id)
                            ON DELETE CASCADE,
    channel_id          NUMERIC NOT NULL
                            CONSTRAINT rhn_actdupchan_chanid_fk
                            REFERENCES rhnChannel (id)
                            ON DELETE CASCADE,
    task                CHAR(1)
                            DEFAULT ('S') NOT NULL
                            CONSTRAINT rhn_actdupchan_task_ck
                                CHECK (task in ('S','U')),
    created             timestamptz default (current_timestamp) not null,
    modified            timestamptz default (current_timestamp) not null
)
;

CREATE UNIQUE INDEX rhn_actdupchan_aid_cid_uq
    ON rhnActionDupChannel (action_dup_id, channel_id);

CREATE INDEX rhn_actdupchan_cid_idx
    ON rhnActionDupChannel (channel_id);

create or replace function rhn_actiondupchan_mod_trig_fun() returns trigger as
$$
begin
	new.modified := current_timestamp;
	return new;
end;
$$ language plpgsql;

create trigger
rhn_actiondupchan_mod_trig
before insert or update on rhnActionDupChannel
for each row
execute procedure rhn_actiondupchan_mod_trig_fun();

