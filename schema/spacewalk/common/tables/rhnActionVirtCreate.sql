--
-- Copyright (c) 2018 SUSE LLC
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

CREATE TABLE rhnActionVirtCreate
(
    action_id            NUMBER NOT NULL
                             CONSTRAINT rhn_action_virt_create_aid_fk
                                 REFERENCES rhnAction (id)
                                 ON DELETE CASCADE
                             CONSTRAINT rhn_action_virt_create_aid_pk
                                 PRIMARY KEY,
    uuid                 VARCHAR2(128),
    vm_type              VARCHAR2(10),
    vm_name              VARCHAR2(256),
    os_type              VARCHAR2(20),
    memory               NUMBER,
    vcpus                NUMBER,
    arch                 VARCHAR2(20),
    graphics_type        VARCHAR2(20),
    remove_disks         CHAR(1),
    remove_interfaces    CHAR(1)
)
ENABLE ROW MOVEMENT
;

CREATE UNIQUE INDEX rhn_action_virt_create_aid_uq
    ON rhnActionVirtCreate (action_id)
    TABLESPACE [[8m_tbs]];
