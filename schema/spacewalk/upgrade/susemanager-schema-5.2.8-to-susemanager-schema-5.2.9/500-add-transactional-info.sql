CREATE TABLE IF NOT EXISTS suseMinionTransactionalInfo
(
    minion_server_id NUMERIC NOT NULL
                         CONSTRAINT suse_minion_transactional_info_sid_fk
                             REFERENCES suseMinionInfo (server_id)
                             ON DELETE CASCADE,
    active_snapshot  NUMERIC,
    default_snapshot NUMERIC,
    snapshots        VARCHAR,
    snapshot_details TEXT,
    snapshot_updated TIMESTAMPTZ,

    CONSTRAINT suse_minion_transactional_info_pk PRIMARY KEY (minion_server_id)
);

CREATE TABLE IF NOT EXISTS suseTransactionalActionHistory
(
    minion_server_id NUMERIC NOT NULL
                         CONSTRAINT suse_transactional_action_history_sid_fk
                             REFERENCES suseMinionInfo (server_id)
                             ON DELETE CASCADE,
    action_id        NUMERIC NOT NULL
                         CONSTRAINT suse_transactional_action_history_aid_fk
                             REFERENCES rhnAction (id)
                             ON DELETE CASCADE,
    created          TIMESTAMPTZ NOT NULL,
    prereq_status    VARCHAR(32) NOT NULL,
    prereq_at        TIMESTAMPTZ,
    reboot_required  BOOLEAN NOT NULL DEFAULT FALSE,
    reboot_status    VARCHAR(32) NOT NULL,
    reboot_at        TIMESTAMPTZ,
    post_status      VARCHAR(32) NOT NULL,
    post_at          TIMESTAMPTZ,

    CONSTRAINT suse_transactional_action_history_pk PRIMARY KEY (minion_server_id, action_id)
);

INSERT INTO rhnActionType (id, label, name, trigger_snapshot, unlocked_only, maintenance_mode_only)
SELECT 528, 'snapshots.refresh_list', 'Refresh Snapshots', 'N', 'N', 'N'
WHERE NOT EXISTS (SELECT 1 FROM rhnActionType WHERE id = 528);

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
SELECT '', '/manager/systems/details/snapshots', 'GET', 'W', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM access.endpoint
    WHERE endpoint = '/manager/systems/details/snapshots' AND http_method = 'GET'
);

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
SELECT '', '/manager/api/systems/:sid/details/snapshots/refresh', 'POST', 'W', TRUE
WHERE NOT EXISTS (
    SELECT 1 FROM access.endpoint
    WHERE endpoint = '/manager/api/systems/:sid/details/snapshots/refresh' AND http_method = 'POST'
);

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/snapshots' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/systems/:sid/details/snapshots/refresh' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;
