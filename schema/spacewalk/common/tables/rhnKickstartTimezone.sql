--
-- Copyright (c) 2008 Red Hat, Inc.
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


CREATE TABLE rhnKickstartTimezone
(
    id            NUMERIC NOT NULL
                      CONSTRAINT rhn_ks_timezone_pk PRIMARY KEY
                      ,
    label         VARCHAR(128) NOT NULL,
    name          VARCHAR(128) NOT NULL,
    install_type  NUMERIC NOT NULL
                      CONSTRAINT rhn_ks_timezone_it_fk
                          REFERENCES rhnKSInstallType (id)
)

;

CREATE UNIQUE INDEX rhn_ks_timezone_it_label_uq
    ON rhnKickstartTimezone (install_type, label)
    ;

CREATE UNIQUE INDEX rhn_ks_timezone_it_name_uq
    ON rhnKickstartTimezone (install_type, name)
    ;

CREATE SEQUENCE rhn_ks_timezone_id_seq;

