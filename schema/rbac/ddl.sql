-- TODO: Review all CASCADE rules
-- TODO: Add descriptions to tables

DROP SCHEMA access CASCADE;

-- Schema
CREATE SCHEMA access;
SET search_path TO access, CURRENT;

-- Tables
CREATE TABLE endpoint (
    id              NUMERIC NOT NULL PRIMARY KEY,
    class_method    VARCHAR(255) NOT NULL,
    endpoint        VARCHAR(255) NOT NULL,
    http_method     VARCHAR(255) NOT NULL,
    scope           CHAR(1) NOT NULL
                        CHECK (scope in ('A', 'W')),
    authorized      BOOLEAN NOT NULL DEFAULT true,
    created         TIMESTAMPTZ NOT NULL DEFAULT (current_timestamp),
    modified        TIMESTAMPTZ NOT NULL DEFAULT (current_timestamp)
);

CREATE SEQUENCE endpoint_id_seq;

CREATE UNIQUE INDEX endpoint_endpoint_http_method_uq
ON endpoint(endpoint, http_method);

/*
CREATE TABLE userGroup
(
    id              NUMERIC NOT NULL PRIMARY KEY,
    name            VARCHAR(64) NOT NULL,
    description     VARCHAR(1024) NOT NULL,
    system_group    CHAR(1) DEFAULT ('N') NOT NULL,
    org_id          NUMERIC
                        REFERENCES public.web_customer(id)
                        ON DELETE CASCADE,
    created          TIMESTAMPTZ NOT NULL DEFAULT (current_timestamp) NOT NULL,
    modified         TIMESTAMPTZ NOT NULL DEFAULT (current_timestamp) NOT NULL
);
-- maybe we can have the system groups as not assign to any organization
-- then for each organization user can define new groups
CREATE UNIQUE INDEX userGroup_orgid_name_uq
    ON userGroup(org_id, name);
*/

CREATE TABLE namespace (
    id          NUMERIC PRIMARY KEY,
    namespace   VARCHAR(255) NOT NULL,
    access_mode CHAR(1) NOT NULL
                    CHECK (access_mode IN ('R', 'W')),
    description TEXT
);

CREATE SEQUENCE namespace_id_seq;

CREATE TABLE endpointNamespace (
    namespace_id    NUMERIC NOT NULL
                        REFERENCES namespace(id)
                        ON DELETE CASCADE,
    endpoint_id     NUMERIC NOT NULL
                        REFERENCES endpoint(id)
                        ON DELETE CASCADE
);

CREATE UNIQUE INDEX endpointNamespace_eid_nid_uq
    ON endpointNamespace(endpoint_id, namespace_id);

CREATE TABLE userNamespace (
    user_id         NUMERIC NOT NULL
                        REFERENCES public.web_contact(id)
                        ON DELETE CASCADE,
    namespace_id    NUMERIC NOT NULL
                        REFERENCES namespace(id)
                        ON DELETE CASCADE
);

CREATE UNIQUE INDEX userNamespace_uid_nid_uq
    ON userNamespace(user_id, namespace_id);

-- Views (for convenience)

CREATE VIEW endpointCatalog AS
SELECT n.namespace, n.access_mode, e.endpoint, e.http_method, e.scope
FROM endpointNamespace en, endpoint e, namespace n
WHERE en.namespace_id = n.id AND en.endpoint_id = e.id;

CREATE VIEW userAccessTable AS
SELECT user_id, namespace, STRING_AGG(access_mode, '') AS access_mode
FROM userNamespace un
JOIN namespace n ON un.namespace_id = n.id
GROUP BY user_id, namespace;

-- Stored procedures (for development)

CREATE OR REPLACE PROCEDURE grant_access (
    uid         NUMERIC,
    ns_pattern  VARCHAR(255),
    mode        CHAR(1) DEFAULT '*'
)
LANGUAGE plpgsql
AS $$
BEGIN
    IF mode = '*' THEN
        INSERT INTO userNamespace(user_id, namespace_id)
        SELECT uid, n.id FROM namespace n
        WHERE n.namespace LIKE ns_pattern
        ON CONFLICT (user_id, namespace_id) DO NOTHING;

    ELSIF UPPER(mode) IN ('R', 'W') THEN
        INSERT INTO userNamespace(user_id, namespace_id)
        SELECT uid, n.id FROM namespace n
        WHERE n.namespace LIKE ns_pattern
        AND n.access_mode = UPPER(mode)
        ON CONFLICT (user_id, namespace_id) DO NOTHING;

    ELSE RAISE EXCEPTION 'Invalid mode: % (must be R, W or *)', mode;
    END IF;
END;
$$;

CREATE OR REPLACE PROCEDURE revoke_access (
    uid         NUMERIC,
    ns_pattern  VARCHAR(255),
    mode        CHAR(1) DEFAULT '*'
)
LANGUAGE plpgsql
AS $$
BEGIN
    IF mode = '*' THEN
        DELETE FROM userNamespace un
        WHERE un.user_id = uid AND un.namespace_id IN
            (SELECT id FROM namespace WHERE namespace LIKE ns_pattern);

    ELSIF UPPER(mode) IN ('R', 'W') THEN
        DELETE FROM userNamespace un
        WHERE un.user_id = uid
        AND un.namespace_id IN (
            SELECT id FROM namespace n
            WHERE n.namespace LIKE ns_pattern
            AND n.access_mode = UPPER(mode)
        );

    ELSE RAISE EXCEPTION 'Invalid mode: % (must be R, W or *)', mode;
    END IF;
END;
$$;
