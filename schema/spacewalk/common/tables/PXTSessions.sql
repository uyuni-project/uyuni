--
-- Copyright (c) 2008--2011 Red Hat, Inc.
-- Copyright (c) 2025 SUSE LLC.
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

CREATE UNLOGGED TABLE PXTSessions
(
    id           BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    web_user_id  NUMERIC
                     CONSTRAINT pxtsessions_user
                         REFERENCES web_contact (id)
                         ON DELETE CASCADE,
    expires      NUMERIC DEFAULT (0) NOT NULL
);

CREATE INDEX PXTSessions_user
    ON PXTSessions (web_user_id);

CREATE INDEX PXTSessions_expires
    ON PXTSessions (expires);