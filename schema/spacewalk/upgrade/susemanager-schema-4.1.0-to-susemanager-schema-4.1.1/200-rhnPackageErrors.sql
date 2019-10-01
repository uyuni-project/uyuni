--
-- Copyright (c) 2019 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

CREATE TABLE IF NOT EXISTS rhnPackageError
(
    id          NUMERIC NOT NULL
                    CONSTRAINT rhn_pkg_errs_id_pk PRIMARY KEY,
    package_id  NUMERIC NOT NULL
                    CONSTRAINT rhn_pkg_err_pid_fk
                       REFERENCES rhnPackage (id)
                       ON DELETE CASCADE,
    error       VARCHAR(2048) NOT NULL,
    created     TIMESTAMPTZ
                     DEFAULT (current_timestamp) NOT NULL
)
;

CREATE SEQUENCE IF NOT EXISTS rhn_package_err_id_seq;