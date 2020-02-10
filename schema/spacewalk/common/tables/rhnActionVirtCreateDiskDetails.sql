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

CREATE TABLE rhnActionVirtCreateDiskDetails
(
    id                   NUMERIC NOT NULL
                             CONSTRAINT rhn_action_virt_create_disk_details_id_pk
                                 PRIMARY KEY,
    type                 VARCHAR(15),
    device               VARCHAR(10),
    template             VARCHAR(256),
    size                 NUMERIC,
    bus                  VARCHAR(10),
    pool                 VARCHAR(256),
    source_file          VARCHAR(1024),
    idx                  NUMERIC,
    action_id            NUMERIC NOT NULL
                             CONSTRAINT rhn_action_virt_create_disk_details_aid_fk
                                 REFERENCES rhnActionVirtCreate (action_id)
                                 ON DELETE CASCADE
)

;

CREATE INDEX rhn_action_virt_create_disk_details_id_idx
    ON rhnActionVirtCreateDiskDetails (id)
    ;

CREATE SEQUENCE rhn_action_virt_create_disk_details_id_seq;
