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
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--

CREATE TABLE suseServerState
(
    id               NUMBER NOT NULL
                         CONSTRAINT suse_server_state_id_pk PRIMARY KEY,
    server_id        NUMBER NOT NULL
                         CONSTRAINT suse_server_state_sid_fk
                             REFERENCES rhnServer (id),
    package_state_group_id NUMBER NOT NULL,
    revision         NUMBER NOT NULL
                         DEFAULT (0),
    created          timestamp with local time zone
                         DEFAULT (current_timestamp) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE SEQUENCE suse_server_state_id_seq;

ALTER TABLE suseServerState
    ADD CONSTRAINT suse_server_state_sid_rev_uq UNIQUE (server_id, revision);
