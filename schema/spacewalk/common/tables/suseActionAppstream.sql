--
-- Copyright (c) 2024 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.

CREATE TABLE suseActionAppstream(
    id          NUMERIC NOT NULL
                CONSTRAINT suse_act_appstream_id_pk PRIMARY KEY,
    action_id   NUMERIC NOT NULL
                CONSTRAINT suse_act_appstream_act_fk
                REFERENCES rhnAction (id) ON DELETE CASCADE,
    module_name VARCHAR(128) NOT NULL,
    stream      VARCHAR(128) NULL,
    type        VARCHAR(10)  NOT NULL,
    created             TIMESTAMPTZ
                            DEFAULT (current_timestamp) NOT NULL,
    modified            TIMESTAMPTZ
                            DEFAULT (current_timestamp) NOT NULL
);

CREATE INDEX suse_act_appstream_aid_idx ON suseActionAppstream (action_id);

CREATE SEQUENCE suse_act_appstream_id_seq;
