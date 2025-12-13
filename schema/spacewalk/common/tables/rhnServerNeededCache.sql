--
-- Copyright (c) 2008--2013 Red Hat, Inc.
-- Copyright (c) 2025 SUSE LLC
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


CREATE TABLE rhnServerNeededCache
(
    server_id   NUMERIC NOT NULL
                    CONSTRAINT rhn_sncp_sid_fk
                        REFERENCES rhnServer (id)
                        ON DELETE CASCADE,
    errata_id   NUMERIC
                    CONSTRAINT rhn_sncp_eid_fk
                        REFERENCES rhnErrata (id)
                        ON DELETE CASCADE,
    package_id  NUMERIC NOT NULL
                    CONSTRAINT rhn_sncp_pid_fk
                        REFERENCES rhnPackage (id)
                        ON DELETE CASCADE,
    channel_id   NUMERIC
                    CONSTRAINT rhn_sncp_cid_fk
                        REFERENCES rhnChannel (id)
                        ON DELETE CASCADE
);

CREATE INDEX rhn_snc_server_idx
    ON rhnServerNeededCache (server_id) ;

CREATE INDEX rhn_snc_pid_idx
    ON rhnServerNeededCache (package_id) ;

CREATE INDEX rhn_snc_eid_idx
    ON rhnServerNeededCache (errata_id) ;

CREATE INDEX rhn_snc_cid_idx
    ON rhnServerNeededCache (channel_id);

CREATE INDEX rhn_snc_seid_idx
    ON rhnServerNeededCache (server_id, errata_id);

CREATE INDEX rhn_snc_speid_idx
    ON rhnServerNeededCache (server_id, package_id, errata_id);
