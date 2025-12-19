--
-- Copyright (c) 2025 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

CREATE TABLE access.namespace (
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    namespace   VARCHAR NOT NULL,
    access_mode CHAR(1) NOT NULL
                    CHECK (access_mode IN ('R', 'W')),
    description TEXT
);
COMMENT ON TABLE access.namespace IS 'Namespace definitions to provide access to';

CREATE UNIQUE INDEX namespace_ns_mode_uq
ON access.namespace(namespace, access_mode);

CREATE INDEX namespace_search_tsvector_idx
ON access.namespace
USING GIN (
    to_tsvector('english', regexp_replace(namespace, '\.', ' ', 'g') || ' ' || coalesce(description, ''))
);

CREATE INDEX namespace_search_trgm_idx
ON access.namespace
USING GIN (lower(regexp_replace(namespace, '\.', ' ', 'g')) gin_trgm_ops);

CREATE INDEX description_search_trgm_idx
ON access.namespace
USING GIN (lower(description) gin_trgm_ops);
