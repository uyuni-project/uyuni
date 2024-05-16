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
                       CONSTRAINT rhn_chsf_cid_pk PRIMARY KEY,
    no_strict   BOOLEAN NOT NULL DEFAULT FALSE,
    no_errata   BOOLEAN NOT NULL DEFAULT FALSE,
    only_latest BOOLEAN NOT NULL DEFAULT FALSE,
    create_tree BOOLEAN NOT NULL DEFAULT FALSE,
    quit_on_error BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT rhn_chsf_cid_fk FOREIGN KEY (channel_id)
                        REFERENCES rhnchannel(id)
                        ON DELETE CASCADE
);
