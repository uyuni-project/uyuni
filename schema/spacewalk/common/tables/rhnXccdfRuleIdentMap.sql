--
-- Copyright (c) 2012 Red Hat, Inc.
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

CREATE TABLE rhnXccdfRuleIdentMap
(
    rresult_id    NUMERIC NOT NULL
                      CONSTRAINT rhn_xccdf_rim_rresult_fk
                          REFERENCES rhnXccdfRuleresult (id)
                          ON DELETE CASCADE,
    ident_id      NUMERIC NOT NULL
                      CONSTRAINT rhn_xccdf_rim_ident_fk
                          REFERENCES rhnXccdfIdent (id)
)

;

CREATE UNIQUE INDEX rhn_xccdf_rim_ri_uq
    ON rhnXccdfRuleIdentMap (rresult_id, ident_id)
    
    ;

CREATE INDEX rhn_xccdf_rim_ident_idx
    ON rhnXccdfRuleIdentMap (ident_id)
    
    ;
