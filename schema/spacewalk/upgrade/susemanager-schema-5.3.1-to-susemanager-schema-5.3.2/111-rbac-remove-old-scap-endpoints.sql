-- Remove old SCAP scheduling Struts web endpoints
DELETE FROM access.endpoint
    WHERE endpoint = '/systems/details/audit/ScheduleXccdf.do'
    AND scope = 'W';

DELETE FROM access.endpoint
    WHERE endpoint = '/systems/ssm/audit/ScheduleXccdf.do'
    AND scope = 'W';

DELETE FROM access.endpoint
    WHERE endpoint = '/systems/ssm/audit/ScheduleXccdfConfirm.do'
    AND scope = 'W';

-- Remove old SCAP scheduling API endpoints
DELETE FROM access.endpoint
    WHERE endpoint = '/manager/api/system/scap/scheduleXccdfScan'
    AND scope = 'A';

DELETE FROM access.endpoint
    WHERE endpoint = '/manager/api/system/scap/scheduleBetaXccdfScanCustom'
    AND scope = 'A';

DELETE FROM access.endpoint
    WHERE endpoint = '/manager/api/system/scap/scheduleBetaXccdfScanWithPolicy'
    AND scope = 'A';

-- Remove old SCAP scheduling API namespaces (CASCADE will remove endpointNamespace and accessGroupNamespace entries)
DELETE FROM access.namespace
    WHERE namespace = 'api.system.scap.schedule_xccdf_scan';

DELETE FROM access.namespace
    WHERE namespace = 'api.system.scap.schedule_beta_xccdf_scan_custom';

DELETE FROM access.namespace
    WHERE namespace = 'api.system.scap.schedule_beta_xccdf_scan_with_policy';

-- Add new SCAP scheduling API endpoints (non-beta versions)
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.system.scap.SystemScapHandler.scheduleXccdfScanWithPolicy',
           '/manager/api/system/scap/scheduleXccdfScanWithPolicy', 'POST', 'A', True
    WHERE NOT EXISTS (
        SELECT 1 FROM access.endpoint
        WHERE endpoint = '/manager/api/system/scap/scheduleXccdfScanWithPolicy' AND http_method = 'POST'
    );

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.system.scap.SystemScapHandler.scheduleXccdfScanCustom',
           '/manager/api/system/scap/scheduleXccdfScanCustom', 'POST', 'A', True
    WHERE NOT EXISTS (
        SELECT 1 FROM access.endpoint
        WHERE endpoint = '/manager/api/system/scap/scheduleXccdfScanCustom' AND http_method = 'POST'
    );

-- Add new SCAP scheduling API namespaces
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.scap.schedule_xccdf_scan_with_policy', 'W', 'Schedule SCAP scan using a policy'
    WHERE NOT EXISTS (
        SELECT 1 FROM access.namespace
        WHERE namespace = 'api.system.scap.schedule_xccdf_scan_with_policy' AND access_mode = 'W'
    );

INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.system.scap.schedule_xccdf_scan_custom', 'W', 'Schedule SCAP scan with custom parameters'
    WHERE NOT EXISTS (
        SELECT 1 FROM access.namespace
        WHERE namespace = 'api.system.scap.schedule_xccdf_scan_custom' AND access_mode = 'W'
    );

-- Map new endpoints to namespaces
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.scap.schedule_xccdf_scan_with_policy' AND ns.access_mode = 'W'
      AND ep.endpoint = '/manager/api/system/scap/scheduleXccdfScanWithPolicy' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.scap.schedule_xccdf_scan_custom' AND ns.access_mode = 'W'
      AND ep.endpoint = '/manager/api/system/scap/scheduleXccdfScanCustom' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

-- Grant access to all access groups
INSERT INTO access.accessGroupNamespace (group_id, namespace_id)
    SELECT ag.id, ns.id FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN (
        'api.system.scap.schedule_xccdf_scan_with_policy',
        'api.system.scap.schedule_xccdf_scan_custom'
    )
    AND ns.access_mode = 'W'
    ON CONFLICT (group_id, namespace_id) DO NOTHING;
