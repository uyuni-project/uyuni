--
-- Copyright (c) 2025 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- SPDX-License-Identifier: GPL-2.0-only

CREATE TABLE suseActionSupportDataDetails (
    id                  BIGINT CONSTRAINT suse_act_suppdata_id_pk PRIMARY KEY
                        GENERATED ALWAYS AS IDENTITY,
    action_id           NUMERIC NOT NULL
                        CONSTRAINT suse_act_suppdata_act_fk
                        REFERENCES rhnAction (id) ON DELETE CASCADE,
    case_number         VARCHAR NOT NULL,
    parameter           VARCHAR NULL,
    upload_geo          upload_geo_t NOT NULL,
    created             TIMESTAMPTZ
                            DEFAULT (current_timestamp) NOT NULL,
    modified            TIMESTAMPTZ
                            DEFAULT (current_timestamp) NOT NULL
);

CREATE INDEX suse_act_suppdata_aid_idx ON suseActionSupportDataDetails (action_id);
