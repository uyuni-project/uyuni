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
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--


CREATE TABLE rhnKickstartDefaults
(
    kickstart_id         NUMERIC NOT NULL
                             CONSTRAINT rhn_ksd_ksid_uq UNIQUE
                             CONSTRAINT rhn_ksd_ksid_fk
                                 REFERENCES rhnKSData (id)
                                 ON DELETE CASCADE,
    kstree_id            NUMERIC NOT NULL
                             CONSTRAINT rhn_ksd_kstid_fk
                                 REFERENCES rhnKickstartableTree (id)
                                 ON DELETE CASCADE,
    server_profile_id    NUMERIC
                             CONSTRAINT rhn_ksd_spid_fk
                                 REFERENCES rhnServerProfile (id)
                                 ON DELETE SET NULL,
    cfg_management_flag  CHAR(1)
                             DEFAULT ('Y') NOT NULL
                             CONSTRAINT rhn_ksd_cmf_ck
                                 CHECK (cfg_management_flag in ('Y','N')),
    remote_command_flag  CHAR(1)
                             DEFAULT ('N') NOT NULL
                             CONSTRAINT rhn_ksd_rmf_ck
                                 CHECK (remote_command_flag in ('Y','N')),
    virtualization_type  NUMERIC NOT NULL
                             CONSTRAINT rhn_ksd_kvt_fk
                                 REFERENCES rhnKickstartVirtualizationType (id)
                                 ON DELETE SET NULL,
    created              TIMESTAMPTZ
                             DEFAULT (current_timestamp) NOT NULL,
    modified             TIMESTAMPTZ
                             DEFAULT (current_timestamp) NOT NULL
)

;

CREATE INDEX rhn_ksd_kstid_idx
    ON rhnKickstartDefaults (kstree_id)
    ;

