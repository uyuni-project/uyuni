--
-- Copyright (c) 2015 SUSE LLC
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

CREATE TABLE suseServerVirtualHostManager
(
    server_id     NUMERIC NOT NULL
                    CONSTRAINT suse_server_vhms_sid_fk
                    REFERENCES rhnServer (id)
                    ON DELETE CASCADE,
    vhmserver_id  NUMERIC NOT NULL
                    CONSTRAINT suse_server_vhms_vhmsid_fk
                    REFERENCES suseVirtualHostManager (id)
                    ON DELETE CASCADE
)

;

CREATE UNIQUE INDEX suse_svhm_sid_vhmid_uq
ON suseServerVirtualHostManager (server_id, vhmserver_id)
;

