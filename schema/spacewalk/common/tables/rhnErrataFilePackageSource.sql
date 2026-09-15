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


CREATE TABLE rhnErrataFilePackageSource
(
    package_id      NUMERIC NOT NULL
                        CONSTRAINT rhn_efileps_pid_fk
                            REFERENCES rhnPackageSource (id)
                            ON DELETE CASCADE,
    errata_file_id  NUMERIC NOT NULL
                        CONSTRAINT rhn_efileps_fileid_fk
                            REFERENCES rhnErrataFile (id)
                            ON DELETE CASCADE,
    created         TIMESTAMPTZ
                        DEFAULT (current_timestamp) NOT NULL,
    modified        TIMESTAMPTZ
                        DEFAULT (current_timestamp) NOT NULL
)

;

CREATE INDEX rhn_efileps_efid_pid_idx
    ON rhnErrataFilePackageSource (errata_file_id, package_id)
    ;

ALTER TABLE rhnErrataFilePackageSource
    ADD CONSTRAINT rhn_efileps_efid_uq UNIQUE (errata_file_id);

