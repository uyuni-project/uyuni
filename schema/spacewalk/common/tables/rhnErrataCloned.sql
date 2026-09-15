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
-- SPDX-License-Identifier: GPL-2.0-only
--
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--


CREATE TABLE rhnErrataCloned
(
    original_id  NUMERIC NOT NULL
                     CONSTRAINT rhn_errataclone_feid_fk
                         REFERENCES rhnErrata (id)
                         ON DELETE CASCADE,
    id           NUMERIC NOT NULL
                     CONSTRAINT rhn_errataclone_teid_fk
                         REFERENCES rhnErrata (id)
                         ON DELETE CASCADE,
    created      TIMESTAMPTZ
                     DEFAULT (current_timestamp) NOT NULL,
    modified     TIMESTAMPTZ
                     DEFAULT (current_timestamp) NOT NULL
)

;

CREATE INDEX rhn_errataclone_feid_teid_idx
    ON rhnErrataCloned (original_id, id)
    ;

ALTER TABLE rhnErrataCloned
    ADD CONSTRAINT rhn_errataclone_feid_teid_uq UNIQUE (original_id, id);

ALTER TABLE rhnErrataCloned
    ADD CONSTRAINT rhn_errataclone_id_pk PRIMARY KEY (id);

