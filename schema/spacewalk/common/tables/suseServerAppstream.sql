--
-- Copyright (c) 2024 SUSE LLC
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
CREATE TABLE suseServerAppstream(
    id  NUMERIC NOT NULL
            CONSTRAINT suse_as_servermodule_id_pk PRIMARY KEY,
    server_id NUMERIC NOT NULL
            REFERENCES rhnServer(id)
            ON DELETE CASCADE,
    name    VARCHAR(128) NOT NULL,
    stream  VARCHAR(128) NOT NULL,
    version VARCHAR(128) NOT NULL,
    context VARCHAR(16) NOT NULL,
    arch    VARCHAR(16) NOT NULL
);

CREATE SEQUENCE suse_as_servermodule_seq;
