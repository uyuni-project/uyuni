--
-- Copyright (c) 2008--2011 Red Hat, Inc.
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


CREATE TABLE rhnOrgErrataCacheQueue
(
    org_id        NUMERIC NOT NULL
                      CONSTRAINT rhn_oecq_oid_fk
                          REFERENCES web_customer (id)
                          ON DELETE CASCADE,
    server_count  NUMERIC NOT NULL,
    processed     NUMERIC
                      DEFAULT (0) NOT NULL
)


;

CREATE UNIQUE INDEX rhn_oecq_oid_uq
    ON rhnOrgErrataCacheQueue (org_id)
    
    ;

