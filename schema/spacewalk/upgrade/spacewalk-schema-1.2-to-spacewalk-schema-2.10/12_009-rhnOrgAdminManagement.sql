--
-- Copyright (c) 2015 Red Hat, Inc.
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

CREATE TABLE rhnOrgAdminManagement
(
    org_id                  NUMERIC NOT NULL
                                CONSTRAINT rhn_orgadm_mngt_fk
                                    REFERENCES web_customer (id)
                                    ON DELETE CASCADE,
    enabled                 CHAR(1)
                                DEFAULT ('Y') NOT NULL
                                CONSTRAINT rhn_orgadm_mngt_enabled_ck
                                    CHECK (enabled in ('Y', 'N')),
    created                 TIMESTAMPTZ
                                DEFAULT (current_timestamp) NOT NULL,
    modified                TIMESTAMPTZ
                                DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_orgadm_mngt_org_id
    on rhnOrgAdminManagement (org_id)
        ;
