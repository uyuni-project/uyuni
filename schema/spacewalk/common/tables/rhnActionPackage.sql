--
-- Copyright (c) 2008--2010 Red Hat, Inc.
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


CREATE TABLE rhnActionPackage
(
    id               NUMERIC NOT NULL
                         CONSTRAINT rhn_act_p_id_pk PRIMARY KEY
                         ,
    action_id        NUMERIC NOT NULL
                         CONSTRAINT rhn_act_p_act_fk
                             REFERENCES rhnAction (id)
                             ON DELETE CASCADE,
    parameter        VARCHAR(128)
                         DEFAULT ('upgrade') NOT NULL
                         CONSTRAINT rhn_act_p_param_ck
                             CHECK (parameter IN ('upgrade', 'install', 'remove', 'downgrade', 'lock')),
    name_id          NUMERIC NOT NULL
                         CONSTRAINT rhn_act_p_name_fk
                             REFERENCES rhnPackageName (id),
    evr_id           NUMERIC
                         CONSTRAINT rhn_act_p_evr_fk
                             REFERENCES rhnPackageEvr (id),
    package_arch_id  NUMERIC
                         CONSTRAINT rhn_act_p_paid_fk
                             REFERENCES rhnPackageArch (id),

    created             TIMESTAMPTZ
                            DEFAULT (current_timestamp) NOT NULL,
    modified            TIMESTAMPTZ
                            DEFAULT (current_timestamp) NOT NULL

)

;

CREATE INDEX rhn_act_p_aid_idx
    ON rhnActionPackage (action_id)
    
    ;

CREATE SEQUENCE rhn_act_p_id_seq;

