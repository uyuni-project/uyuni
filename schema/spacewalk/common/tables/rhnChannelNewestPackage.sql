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
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--


CREATE TABLE rhnChannelNewestPackage
(
    channel_id      NUMERIC NOT NULL
                        CONSTRAINT rhn_cnp_cid_fk
                            REFERENCES rhnChannel (id)
                            ON DELETE CASCADE,
    name_id         NUMERIC NOT NULL
                        CONSTRAINT rhn_cnp_nid_fk
                            REFERENCES rhnPackageName (id),
    evr_id          NUMERIC NOT NULL
                        CONSTRAINT rhn_cnp_eid_fk
                            REFERENCES rhnPackageEVR (id),
    package_arch_id NUMERIC NOT NULL
                        CONSTRAINT rhn_cnp_paid_fk
                            REFERENCES rhnPackageArch (id),
    package_id      NUMERIC NOT NULL
                        CONSTRAINT rhn_cnp_pid_fk
                            REFERENCES rhnPackage (id)
                            ON DELETE CASCADE,
    appstream_id    NUMERIC
                        CONSTRAINT rhn_cnp_aid_fk
                            REFERENCES suseAppstream (id)
                            ON DELETE CASCADE
)

;


CREATE INDEX rhn_cnp_pid_idx
    ON rhnChannelNewestPackage (package_id)
    ;

CREATE UNIQUE INDEX rhn_cnp_cid_nid_aid_uq
    ON rhnchannelnewestpackage (channel_id, name_id, package_arch_id, appstream_id)
    WHERE appstream_id IS NOT NULL;

CREATE UNIQUE INDEX rhn_cnp_cid_nid_uq
    ON rhnchannelnewestpackage (channel_id, name_id, package_arch_id)
    WHERE appstream_id IS NULL;
