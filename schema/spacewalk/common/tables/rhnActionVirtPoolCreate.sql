--
-- Copyright (c) 2020 SUSE LLC
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

CREATE TABLE rhnActionVirtPoolCreate
(
    action_id            NUMERIC NOT NULL
                             CONSTRAINT rhn_action_virt_pool_create_aid_fk
                                 REFERENCES rhnAction (id)
                                 ON DELETE CASCADE
                             CONSTRAINT rhn_action_virt_pool_create_aid_pk
                                 PRIMARY KEY,
    pool_name            VARCHAR(256),
    uuid                 VARCHAR(128),
    type                 VARCHAR(25),
    target               VARCHAR(256),
    autostart            CHAR(1)
                            DEFAULT ('Y') NOT NULL
                            CONSTRAINT rhn_avpcreate_autostart_ck
                                CHECK (autostart in ('Y','N')),
    permission_mode                 VARCHAR(4),
    permission_owner                VARCHAR(10),
    permission_group                VARCHAR(10),
    permission_seclabel             VARCHAR(256),
    source               VARCHAR(2048)
)
;

CREATE UNIQUE INDEX rhn_action_virt_pool_create_aid_uq
    ON rhnActionVirtPoolCreate (action_id);

