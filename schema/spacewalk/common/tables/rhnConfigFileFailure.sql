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


CREATE TABLE rhnConfigFileFailure
(
    id        NUMERIC NOT NULL
                  CONSTRAINT rhn_conffile_fail_id_pk PRIMARY KEY
                  ,
    label     VARCHAR(64) NOT NULL
                  CONSTRAINT rhn_conffile_fail_label_uq UNIQUE
                  ,
    name      VARCHAR(256) NOT NULL
                  CONSTRAINT rhn_conffile_fail_name_uq UNIQUE
                  ,
    created   TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL,
    modified  TIMESTAMPTZ
                  DEFAULT (current_timestamp) NOT NULL
)

;

CREATE SEQUENCE rhn_conffile_failure_id_seq;

