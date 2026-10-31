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


CREATE TABLE rhnOrgChannelSettings
(
    org_id      NUMERIC NOT NULL
                    CONSTRAINT rhn_orgcsettings_oid_fk
                        REFERENCES web_customer (id)
                        ON DELETE CASCADE,
    channel_id  NUMERIC NOT NULL
                    CONSTRAINT rhn_orgcsettings_cid_fk
                        REFERENCES rhnChannel (id)
                        ON DELETE CASCADE,
    setting_id  NUMERIC NOT NULL
                    CONSTRAINT rhn_orgcsettings_sid_fk
                        REFERENCES rhnOrgChannelSettingsType (id),
    created     TIMESTAMPTZ
                    DEFAULT (current_timestamp) NOT NULL,
    modified    TIMESTAMPTZ
                    DEFAULT (current_timestamp) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_orgcsettings_oid_cid_uq
    ON rhnOrgChannelSettings (org_id, channel_id, setting_id)
    ;

