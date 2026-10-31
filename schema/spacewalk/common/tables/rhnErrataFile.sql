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


CREATE TABLE rhnErrataFile
(
    id         NUMERIC NOT NULL,
    errata_id  NUMERIC NOT NULL
                   CONSTRAINT rhn_erratafile_errata_fk
                       REFERENCES rhnErrata (id)
                       ON DELETE CASCADE,
    type       NUMERIC NOT NULL
                   CONSTRAINT rhn_erratafile_type_fk
                       REFERENCES rhnErrataFileType (id),
    checksum_id NUMERIC NOT NULL
                   CONSTRAINT rhn_erratafile_chsum_fk
                       REFERENCES rhnChecksum (id),
    filename   VARCHAR(4000) NOT NULL,
    created    TIMESTAMPTZ
                   DEFAULT (current_timestamp) NOT NULL,
    modified   TIMESTAMPTZ
                   DEFAULT (current_timestamp) NOT NULL
)

;

CREATE INDEX rhn_erratafile_id_idx
    ON rhnErrataFile (id)
    ;

CREATE INDEX rhn_erratafile_eid_file_idx
    ON rhnErrataFile (errata_id, filename)
    ;

CREATE SEQUENCE rhn_erratafile_id_seq;

ALTER TABLE rhnErrataFile
    ADD CONSTRAINT rhn_erratafile_id_pk PRIMARY KEY (id);

ALTER TABLE rhnErrataFile
    ADD CONSTRAINT rhn_erratafile_eid_file_uq UNIQUE (errata_id, filename);

