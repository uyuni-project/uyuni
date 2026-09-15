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


CREATE TABLE rhnPackageFile
(
    package_id     NUMERIC NOT NULL
                       CONSTRAINT rhn_package_file_pid_fk
                           REFERENCES rhnPackage (id)
                           ON DELETE CASCADE,
    capability_id  NUMERIC NOT NULL
                       CONSTRAINT rhn_package_file_cid_fk
                           REFERENCES rhnPackageCapability (id),
    device         NUMERIC NOT NULL,
    inode          NUMERIC NOT NULL,
    file_mode      NUMERIC NOT NULL,
    username       VARCHAR(32) NOT NULL,
    groupname      VARCHAR(32) NOT NULL,
    rdev           NUMERIC NOT NULL,
    file_size      NUMERIC NOT NULL,
    mtime          TIMESTAMPTZ NOT NULL,
    checksum_id    NUMERIC
                      CONSTRAINT rhn_package_file_chsum_fk
                          REFERENCES rhnChecksum (id),
    linkto         VARCHAR(256),
    flags          NUMERIC NOT NULL,
    verifyflags    NUMERIC NOT NULL,
    lang           VARCHAR(32),
    created        TIMESTAMPTZ
                       DEFAULT (current_timestamp) NOT NULL,
    modified       TIMESTAMPTZ
                       DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_package_file_pid_cid_uq
    ON rhnPackageFile (package_id, capability_id)
    ;

CREATE INDEX rhn_package_file_cid_idx
    ON rhnPackageFile (capability_id)
    
    ;

