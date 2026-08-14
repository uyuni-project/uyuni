CREATE TABLE IF NOT EXISTS suseMinionTransactionalInfo
(
    minion_server_id NUMERIC NOT NULL
                         CONSTRAINT suse_minion_transactional_info_sid_fk
                             REFERENCES suseMinionInfo (server_id)
                             ON DELETE CASCADE,
    active_snapshot  NUMERIC,
    default_snapshot NUMERIC,
    snapshot_details JSONB,
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
    snapshot_refresh_action_id NUMERIC
                         CONSTRAINT suse_transactional_action_history_refresh_aid_fk
                             REFERENCES rhnAction (id)
                             ON DELETE SET NULL,
    created          TIMESTAMPTZ NOT NULL,
    prereq_status    VARCHAR(32) NOT NULL,
    prereq_at        TIMESTAMPTZ,
    prereq_result    TEXT,
    reboot_required  BOOLEAN NOT NULL DEFAULT FALSE,
    reboot_status    VARCHAR(32) NOT NULL,
    reboot_at        TIMESTAMPTZ,
    after_reboot_status    VARCHAR(32) NOT NULL,
    after_reboot_status_at TIMESTAMPTZ,

    CONSTRAINT suse_transactional_action_history_pk PRIMARY KEY (minion_server_id, action_id),
    CONSTRAINT suse_transactional_action_history_refresh_uq UNIQUE (snapshot_refresh_action_id)
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
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.getSnapshotInfo',
            '/manager/api/system/getSnapshotInfo', 'GET', 'A', TRUE)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleSnapshotRefresh',
            '/manager/api/system/scheduleSnapshotRefresh', 'POST', 'A', TRUE)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.get_snapshot_info', 'R', 'Returns Btrfs snapshot information for a system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;

INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.schedule_snapshot_refresh', 'W',
            'Schedule a Btrfs snapshot information refresh for a system.')
    ON CONFLICT (namespace, access_mode) DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.snapshots' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/snapshots' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.get_snapshot_info' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/system/getSnapshotInfo' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_snapshot_refresh' AND ns.access_mode = 'W'
    AND ep.endpoint = '/manager/api/system/scheduleSnapshotRefresh' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

INSERT INTO access.accessGroupNamespace (group_id, namespace_id)
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN ('api.system.get_snapshot_info', 'api.system.schedule_snapshot_refresh')
    ON CONFLICT (group_id, namespace_id) DO NOTHING;
