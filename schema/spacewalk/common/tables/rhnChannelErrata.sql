--
-- Copyright (c) 2008--2012 Red Hat, Inc.
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
--


CREATE TABLE rhnChannelErrata
(
    channel_id  NUMERIC NOT NULL
                    CONSTRAINT rhn_ce_cid_fk
                        REFERENCES rhnChannel (id)
                        ON DELETE CASCADE,
    errata_id   NUMERIC NOT NULL
                    CONSTRAINT rhn_ce_eid_fk
                        REFERENCES rhnErrata (id)
                        ON DELETE CASCADE,
    created     TIMESTAMPTZ
                    DEFAULT (current_timestamp) NOT NULL,
    modified    TIMESTAMPTZ
                    DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_ce_ce_uq
    ON rhnChannelErrata (channel_id, errata_id)
    ;

CREATE INDEX rhn_ce_eid_idx
    ON rhnChannelErrata (errata_id)
    ;

