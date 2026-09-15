--
-- Copyright (c) 2021 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- SPDX-License-Identifier: GPL-2.0-only
--
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--

CREATE TABLE IF NOT EXISTS rhnActionPackageDetails
(
    id         NUMERIC NOT NULL
               CONSTRAINT rhn_actionpd_id_pk PRIMARY KEY,
    action_id  NUMERIC NOT NULL
                   CONSTRAINT rhn_act_eu_act_fk
                       REFERENCES rhnAction (id)
                       ON DELETE CASCADE,
    allow_vendor_change  CHAR(1) DEFAULT ('N') NOT NULL 
    CONSTRAINT rhn_actdet_avc_ck CHECK (allow_vendor_change in ('Y','N')),

    created   TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL,
    modified  TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL

);

CREATE INDEX IF NOT EXISTS rhn_act_eud_aid_idx
    ON rhnActionPackageDetails (action_id);

CREATE SEQUENCE IF NOT EXISTS rhn_actiondpd_id_seq;

create or replace function rhn_actionpackagedetails_mod_trig_fun() returns trigger as
$$
begin
        new.modified := current_timestamp;
        return new;
end;
$$ language plpgsql;

drop trigger if exists rhn_actionpackagedetails_mod_trig on rhnActionPackageDetails;
create trigger
rhn_actionpackagedetails_mod_trig
before insert or update on rhnActionPackageDetails
for each row
execute procedure rhn_actionpackagedetails_mod_trig_fun();
