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

CREATE SCHEMA IF NOT EXISTS access;
COMMENT ON SCHEMA access IS 'Contains the entities for RBAC';

CREATE TABLE IF NOT EXISTS access.endpoint (
    id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    class_method    VARCHAR NOT NULL,
    endpoint        VARCHAR NOT NULL,
    http_method     VARCHAR NOT NULL,
    scope           CHAR(1) NOT NULL
                        CHECK (scope in ('A', 'W')),
    authorized      BOOLEAN NOT NULL DEFAULT true,
    created         TIMESTAMPTZ NOT NULL DEFAULT (current_timestamp),
    modified        TIMESTAMPTZ NOT NULL DEFAULT (current_timestamp)
);

CREATE UNIQUE INDEX IF NOT EXISTS endpoint_endpoint_http_method_uq
ON access.endpoint(endpoint, http_method);

CREATE TABLE IF NOT EXISTS access.namespace (
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    namespace   VARCHAR NOT NULL,
    access_mode CHAR(1) NOT NULL
                    CHECK (access_mode IN ('R', 'W')),
    description TEXT
);

CREATE TABLE IF NOT EXISTS access.endpointNamespace (
    namespace_id    BIGINT NOT NULL
                        REFERENCES access.namespace(id)
                        ON DELETE CASCADE,
    endpoint_id     BIGINT NOT NULL
                        REFERENCES access.endpoint(id)
                        ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS endpointNamespace_eid_nid_uq
    ON access.endpointNamespace(endpoint_id, namespace_id);

CREATE TABLE IF NOT EXISTS access.userNamespace (
    user_id         NUMERIC NOT NULL
                        REFERENCES public.web_contact(id)
                        ON DELETE CASCADE,
    namespace_id    BIGINT NOT NULL
                        REFERENCES access.namespace(id)
                        ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS userNamespace_uid_nid_uq
    ON access.userNamespace(user_id, namespace_id);


CREATE OR REPLACE VIEW access.endpointCatalog AS
SELECT n.namespace, n.access_mode, e.endpoint, e.http_method, e.scope
FROM access.endpointNamespace en, access.endpoint e, access.namespace n
WHERE en.namespace_id = n.id AND en.endpoint_id = e.id;


CREATE OR REPLACE VIEW access.userAccessTable AS
SELECT user_id, namespace, STRING_AGG(access_mode, '') AS access_mode
FROM access.userNamespace un
JOIN access.namespace n ON un.namespace_id = n.id
GROUP BY user_id, namespace;

