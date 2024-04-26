--
-- Copyright (c) 2023 SUSE LLC
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


CREATE TABLE IF NOT EXISTS suseAppstream(
    id              NUMERIC NOT NULL
                        CONSTRAINT suse_as_module_id_pk PRIMARY KEY,
    channel_id      NUMERIC NOT NULL
                        REFERENCES rhnChannel(id)
                        ON DELETE CASCADE,
    name            VARCHAR(128) NOT NULL,
    stream          VARCHAR(128) NOT NULL,
    version         VARCHAR(128) NOT NULL,
    context         VARCHAR(16) NOT NULL,
    arch            VARCHAR(16) NOT NULL,
    created         TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL,
    modified        TIMESTAMPTZ DEFAULT (current_timestamp) NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_uq_as_module_nsvca
    ON suseAppstream(channel_id, name, stream, version, context, arch);

CREATE SEQUENCE IF NOT EXISTS suse_as_module_seq;
