--
-- Copyright (c) 2008 Red Hat, Inc.
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


CREATE TABLE rhnUserInfoPane
(
    user_id  NUMERIC NOT NULL
                 CONSTRAINT rhn_usr_info_pane_uid_fk
                     REFERENCES web_contact (id)
                     ON DELETE CASCADE,
    pane_id  NUMERIC NOT NULL
                 CONSTRAINT rhn_usr_info_pane_pid_fk
                     REFERENCES rhnInfoPane (id)
                     ON DELETE CASCADE
)

;

CREATE UNIQUE INDEX rhnusrinfopane_uid_pid_uq
    ON rhnUserInfoPane (user_id, pane_id)
    ;

