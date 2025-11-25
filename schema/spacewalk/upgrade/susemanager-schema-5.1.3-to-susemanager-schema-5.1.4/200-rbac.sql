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
-- SPDX-License-Identifier: GPL-2.0-only
--

-- 'access' schema
CREATE SCHEMA IF NOT EXISTS access;
COMMENT ON SCHEMA access IS 'Entities for RBAC';


-- 'accessGroup' table
CREATE TABLE IF NOT EXISTS access.accessGroup (
    id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    org_id          BIGINT REFERENCES web_customer(id)
                        ON DELETE CASCADE,
    label           VARCHAR NOT NULL,
    description     VARCHAR NOT NULL,
    created         TIMESTAMPTZ NOT NULL DEFAULT (current_timestamp),
    modified        TIMESTAMPTZ NOT NULL DEFAULT (current_timestamp)
);
COMMENT ON TABLE access.accessGroup IS 'Access groups (roles) for RBAC';

CREATE UNIQUE INDEX IF NOT EXISTS access_group_label_uq
ON access.accessGroup(label);


-- 'endpoint' table
CREATE TABLE IF NOT EXISTS access.endpoint (
    id              BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    class_method    VARCHAR NOT NULL,
    endpoint        VARCHAR NOT NULL,
    http_method     VARCHAR NOT NULL,
    scope           CHAR(1) NOT NULL
                        CHECK (scope in ('A', 'W')),
    auth_required   BOOLEAN NOT NULL DEFAULT true,
    created         TIMESTAMPTZ NOT NULL DEFAULT (current_timestamp),
    modified        TIMESTAMPTZ NOT NULL DEFAULT (current_timestamp)
);
COMMENT ON TABLE access.endpoint IS 'Web endpoint mappings for RBAC';

CREATE UNIQUE INDEX IF NOT EXISTS endpoint_endpoint_http_method_uq
ON access.endpoint(endpoint, http_method);


-- 'namespace' table
CREATE TABLE IF NOT EXISTS access.namespace (
    id          BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    namespace   VARCHAR NOT NULL,
    access_mode CHAR(1) NOT NULL
                    CHECK (access_mode IN ('R', 'W')),
    description TEXT
);
COMMENT ON TABLE access.namespace IS 'Namespace definitions to provide access to';

CREATE UNIQUE INDEX IF NOT EXISTS namespace_ns_mode_uq
ON access.namespace(namespace, access_mode);


-- 'accessGroupNamespace' table
CREATE TABLE IF NOT EXISTS access.accessGroupNamespace (
    group_id        BIGINT NOT NULL
                        REFERENCES access.accessGroup(id)
                        ON DELETE CASCADE,
    namespace_id    BIGINT NOT NULL
                        REFERENCES access.namespace(id)
                        ON DELETE CASCADE
);
COMMENT ON TABLE access.accessGroupNamespace IS 'Namespace permissions to access groups';

CREATE UNIQUE INDEX IF NOT EXISTS accessGroupNamespace_gid_nid_uq
ON access.accessGroupNamespace(group_id, namespace_id);


-- 'endpointNamespace' table
CREATE TABLE IF NOT EXISTS access.endpointNamespace (
    namespace_id    BIGINT NOT NULL
                        REFERENCES access.namespace(id)
                        ON DELETE CASCADE,
    endpoint_id     BIGINT NOT NULL
                        REFERENCES access.endpoint(id)
                        ON DELETE CASCADE
);
COMMENT ON TABLE access.endpointNamespace IS 'Endpoints grouped into namespaces';

CREATE UNIQUE INDEX IF NOT EXISTS endpointNamespace_eid_nid_uq
ON access.endpointNamespace(endpoint_id, namespace_id);


-- 'userNamespace' table
CREATE TABLE IF NOT EXISTS access.userNamespace (
    user_id         NUMERIC NOT NULL
                        REFERENCES public.web_contact(id)
                        ON DELETE CASCADE,
    namespace_id    BIGINT NOT NULL
                        REFERENCES access.namespace(id)
                        ON DELETE CASCADE
);
COMMENT ON TABLE access.userNamespace IS 'Direct user permissions to namespaces';

CREATE UNIQUE INDEX IF NOT EXISTS userNamespace_uid_nid_uq
ON access.userNamespace(user_id, namespace_id);


-- 'userAccessGroup' table
CREATE TABLE IF NOT EXISTS access.userAccessGroup (
    user_id         NUMERIC NOT NULL
                        REFERENCES public.web_contact(id)
                        ON DELETE CASCADE,
    group_id        BIGINT NOT NULL
                        REFERENCES access.accessGroup(id)
                        ON DELETE CASCADE
);
COMMENT ON TABLE access.userAccessGroup IS 'User access group memberships';

CREATE UNIQUE INDEX IF NOT EXISTS userAccessGroup_uid_gid_uq
ON access.userAccessGroup(user_id, group_id);



-- 'endpointCatalog' view
CREATE OR REPLACE VIEW access.endpointCatalog AS
SELECT n.namespace, n.access_mode, e.endpoint, e.http_method, e.scope
FROM access.endpoint e
JOIN access.endpointNamespace en ON e.id = en.endpoint_id
JOIN access.namespace n ON en.namespace_id = n.id;

COMMENT ON VIEW access.endpointCatalog IS 'Convenience view for browsing endpoints and namespaces';

-- 'userAccessTable' view
CREATE OR REPLACE VIEW access.userAccessTable AS
    WITH endpoints AS (
        SELECT e.*, en.namespace_id
        FROM access.endpoint e
        JOIN access.endpointNamespace en ON e.id = en.endpoint_id
    )

    -- Select endpoints permitted directly to the user
    SELECT e.*, un.user_id
    FROM endpoints e
    JOIN access.userNamespace un ON e.namespace_id = un.namespace_id

    UNION

    -- Select endpoints permitted through an access group
    SELECT e.*, ug.user_id
    FROM endpoints e
    JOIN access.accessGroupNamespace gn ON e.namespace_id = gn.namespace_id
    JOIN access.userAccessGroup ug ON gn.group_id = ug.group_id
;

COMMENT ON VIEW access.userAccessTable IS 'User access rules on endpoints either permitted directly or through an access group';

