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
--

CREATE TABLE rhnChannelSyncFlag (

    channel_id         NUMERIC NOT NULL
    CONSTRAINT rhn_chsf_cid_pk PRIMARY KEY
        REFERENCES rhnchannel(id),
    
    no_strict          BOOLEAN DEFAULT FALSE NOT NULL
        CONSTRAINT rhn_chsf_no_strict_ck
            CHECK (no_strict IN (TRUE, FALSE)),

    no_errata          BOOLEAN DEFAULT FALSE NOT NULL
        CONSTRAINT rhn_chsf_no_errata_ck
            CHECK (no_errata IN (TRUE, FALSE)),

    only_latest        BOOLEAN DEFAULT FALSE NOT NULL
        CONSTRAINT rhn_chsf_only_latest_ck
            CHECK (only_latest IN (TRUE, FALSE)),

    create_tree        BOOLEAN DEFAULT FALSE NOT NULL
        CONSTRAINT rhn_chsf_create_tree_ck
            CHECK (create_tree IN (TRUE, FALSE)),

    quit_on_error      BOOLEAN DEFAULT FALSE NOT NULL
        CONSTRAINT rhn_chsf_quit_on_error_ck
            CHECK (quit_on_error IN (TRUE, FALSE))
);
