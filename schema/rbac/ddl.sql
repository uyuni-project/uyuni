-- Stored procedures (for development)
CREATE OR REPLACE PROCEDURE access.grant_access (
    uid         NUMERIC,
    ns_pattern  VARCHAR(255),
    mode        CHAR(1) DEFAULT '*'
)
LANGUAGE plpgsql
AS $$
BEGIN
    IF mode = '*' THEN
        INSERT INTO access.userNamespace(user_id, namespace_id)
        SELECT uid, n.id FROM access.namespace n
        WHERE n.namespace LIKE ns_pattern
        ON CONFLICT (user_id, namespace_id) DO NOTHING;

    ELSIF UPPER(mode) IN ('R', 'W') THEN
        INSERT INTO access.userNamespace(user_id, namespace_id)
        SELECT uid, n.id FROM access.namespace n
        WHERE n.namespace LIKE ns_pattern
        AND n.access_mode = UPPER(mode)
        ON CONFLICT (user_id, namespace_id) DO NOTHING;

    ELSE RAISE EXCEPTION 'Invalid mode: % (must be R, W or *)', mode;
    END IF;
END;
$$;

CREATE OR REPLACE PROCEDURE access.revoke_access (
    uid         NUMERIC,
    ns_pattern  VARCHAR(255),
    mode        CHAR(1) DEFAULT '*'
)
LANGUAGE plpgsql
AS $$
BEGIN
    IF mode = '*' THEN
        DELETE FROM access.userNamespace un
        WHERE un.user_id = uid AND un.namespace_id IN
            (SELECT id FROM access.namespace WHERE namespace LIKE ns_pattern);

    ELSIF UPPER(mode) IN ('R', 'W') THEN
        DELETE FROM access.userNamespace un
        WHERE un.user_id = uid
        AND un.namespace_id IN (
            SELECT id FROM access.namespace n
            WHERE n.namespace LIKE ns_pattern
            AND n.access_mode = UPPER(mode)
        );

    ELSE RAISE EXCEPTION 'Invalid mode: % (must be R, W or *)', mode;
    END IF;
END;
$$;
