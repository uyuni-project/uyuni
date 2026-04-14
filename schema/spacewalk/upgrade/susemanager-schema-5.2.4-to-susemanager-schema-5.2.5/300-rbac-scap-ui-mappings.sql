-- =============================================================================
--  XML-RPC System SCAP API
-- =============================================================================

-- 1. listScapContent [GET]
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.scap.SystemScapHandler.listScapContent', '/manager/api/system/scap/listScapContent', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.scap.list_scap_content', 'R', 'Lists SCAP content for a given system')
    ON CONFLICT (namespace, access_mode) DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.scap.list_scap_content' AND ns.access_mode = 'R'
      AND ep.endpoint = '/manager/api/system/scap/listScapContent' AND ep.http_method = 'GET'
    ON CONFLICT (endpoint_id, namespace_id) DO NOTHING;

INSERT INTO access.accessGroupNamespace (group_id, namespace_id)
    SELECT ag.id, ns.id FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace = 'api.system.scap.list_scap_content' AND ns.access_mode = 'R'
    ON CONFLICT (group_id, namespace_id) DO NOTHING;

-- 2. listPolicies [GET]
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.scap.SystemScapHandler.listPolicies', '/manager/api/system/scap/listPolicies', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.scap.list_policies', 'R', 'Lists SCAP policies for a given system')
    ON CONFLICT (namespace, access_mode) DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.scap.list_policies' AND ns.access_mode = 'R'
      AND ep.endpoint = '/manager/api/system/scap/listPolicies' AND ep.http_method = 'GET'
    ON CONFLICT (endpoint_id, namespace_id) DO NOTHING;

INSERT INTO access.accessGroupNamespace (group_id, namespace_id)
    SELECT ag.id, ns.id FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace = 'api.system.scap.list_policies' AND ns.access_mode = 'R'
    ON CONFLICT (group_id, namespace_id) DO NOTHING;

-- 3. listTailoringFiles [GET]
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.scap.SystemScapHandler.listTailoringFiles', '/manager/api/system/scap/listTailoringFiles', 'GET', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.scap.list_tailoring_files', 'R', 'Lists SCAP tailoring files for a given system')
    ON CONFLICT (namespace, access_mode) DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.scap.list_tailoring_files' AND ns.access_mode = 'R'
      AND ep.endpoint = '/manager/api/system/scap/listTailoringFiles' AND ep.http_method = 'GET'
    ON CONFLICT (endpoint_id, namespace_id) DO NOTHING;

INSERT INTO access.accessGroupNamespace (group_id, namespace_id)
    SELECT ag.id, ns.id FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace = 'api.system.scap.list_tailoring_files' AND ns.access_mode = 'R'
    ON CONFLICT (group_id, namespace_id) DO NOTHING;

-- 4. scheduleBetaXccdfScanCustom [POST]
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.scap.SystemScapHandler.scheduleBetaXccdfScanCustom', '/manager/api/system/scap/scheduleBetaXccdfScanCustom', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.scap.schedule_beta_xccdf_scan_custom', 'W', 'Schedules a custom XCCDF scan')
    ON CONFLICT (namespace, access_mode) DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.scap.schedule_beta_xccdf_scan_custom' AND ns.access_mode = 'W'
      AND ep.endpoint = '/manager/api/system/scap/scheduleBetaXccdfScanCustom' AND ep.http_method = 'POST'
    ON CONFLICT (endpoint_id, namespace_id) DO NOTHING;

INSERT INTO access.accessGroupNamespace (group_id, namespace_id)
    SELECT ag.id, ns.id FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace = 'api.system.scap.schedule_beta_xccdf_scan_custom' AND ns.access_mode = 'W'
    ON CONFLICT (group_id, namespace_id) DO NOTHING;

-- 5. scheduleBetaXccdfScanWithPolicy [POST]
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('com.redhat.rhn.frontend.xmlrpc.system.scap.SystemScapHandler.scheduleBetaXccdfScanWithPolicy', '/manager/api/system/scap/scheduleBetaXccdfScanWithPolicy', 'POST', 'A', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('api.system.scap.schedule_beta_xccdf_scan_with_policy', 'W', 'Schedules a XCCDF scan using a specific policy')
    ON CONFLICT (namespace, access_mode) DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.scap.schedule_beta_xccdf_scan_with_policy' AND ns.access_mode = 'W'
      AND ep.endpoint = '/manager/api/system/scap/scheduleBetaXccdfScanWithPolicy' AND ep.http_method = 'POST'
    ON CONFLICT (endpoint_id, namespace_id) DO NOTHING;

INSERT INTO access.accessGroupNamespace (group_id, namespace_id)
    SELECT ag.id, ns.id FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace = 'api.system.scap.schedule_beta_xccdf_scan_with_policy' AND ns.access_mode = 'W'
    ON CONFLICT (group_id, namespace_id) DO NOTHING;

-- =============================================================================
-- FEATURE: Recurring Action Policies (Web UI)
-- =============================================================================

-- 1. Recurring Actions Policies [GET]

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/recurringactions/policies', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

-- Mapping to 'systems.groups.recurring'
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.groups.recurring' AND ns.access_mode = 'W'
      AND ep.endpoint = '/manager/api/recurringactions/policies' AND ep.http_method = 'GET'
    ON CONFLICT (endpoint_id, namespace_id) DO NOTHING;

-- Mapping to 'systems.recurring'
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.recurring' AND ns.access_mode = 'W'
      AND ep.endpoint = '/manager/api/recurringactions/policies' AND ep.http_method = 'GET'
    ON CONFLICT (endpoint_id, namespace_id) DO NOTHING;

-- Mapping to 'home.account.myorg.recurring'
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'home.account.myorg.recurring' AND ns.access_mode = 'W'
      AND ep.endpoint = '/manager/api/recurringactions/policies' AND ep.http_method = 'GET'
    ON CONFLICT (endpoint_id, namespace_id) DO NOTHING;


-- =============================================================================
-- FEATURE: SCAP Content Management 
-- =============================================================================

-- Unified Namespace for SCAP Management
INSERT INTO access.namespace (namespace, access_mode, description) VALUES 
    ('audit.scap.management', 'R', 'View SCAP content, tailoring files, and policies'),
    ('audit.scap.management', 'W', 'Create, edit, or delete SCAP content, tailoring files, and policies')
ON CONFLICT (namespace, access_mode) DO NOTHING;

-- =============================================================================
-- FEATURE: SCAP Content Management - Content
-- =============================================================================

-- 1. Endpoints: Content UI
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required) VALUES
    ('', '/manager/audit/scap/content', 'GET', 'W', True),
    ('', '/manager/audit/scap/content/create', 'GET', 'W', True),
    ('', '/manager/audit/scap/content/edit/:id', 'GET', 'W', True),
    ('', '/manager/api/audit/scap/content/create', 'POST', 'W', True),
    ('', '/manager/api/audit/scap/content/update', 'POST', 'W', True),
    ('', '/manager/api/audit/scap/content/delete', 'POST', 'W', True)
ON CONFLICT (endpoint, http_method) DO NOTHING;

-- 2. Mapping: Content READ
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.scap.management' AND ns.access_mode = 'R'
      AND ep.endpoint = '/manager/audit/scap/content' AND ep.http_method = 'GET'
ON CONFLICT (endpoint_id, namespace_id) DO NOTHING;

-- 3. Mapping: Content WRITE
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.scap.management' AND ns.access_mode = 'W'
      AND ep.endpoint IN (
        '/manager/audit/scap/content/create', 
        '/manager/audit/scap/content/edit/:id',
        '/manager/api/audit/scap/content/create',
        '/manager/api/audit/scap/content/update',
        '/manager/api/audit/scap/content/delete'
      )
ON CONFLICT (endpoint_id, namespace_id) DO NOTHING;

-- =============================================================================
-- FEATURE: SCAP Content Management - Tailoring Files
-- =============================================================================

-- 1. Endpoints: Tailoring UI
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required) VALUES
    ('', '/manager/audit/scap/tailoring-files', 'GET', 'W', True),
    ('', '/manager/audit/scap/tailoring-file/create', 'GET', 'W', True),
    ('', '/manager/audit/scap/tailoring-file/edit/:id', 'GET', 'W', True),
    ('', '/manager/api/audit/scap/tailoring-file/create', 'POST', 'W', True),
    ('', '/manager/api/audit/scap/tailoring-file/update', 'POST', 'W', True),
    ('', '/manager/api/audit/scap/tailoring-file/delete', 'POST', 'W', True)
ON CONFLICT (endpoint, http_method) DO NOTHING;

-- 2. Mapping: Tailoring READ
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.scap.management' AND ns.access_mode = 'R'
      AND ep.endpoint = '/manager/audit/scap/tailoring-files' AND ep.http_method = 'GET'
ON CONFLICT (endpoint_id, namespace_id) DO NOTHING;

-- 3. Mapping: Tailoring WRITE
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.scap.management' AND ns.access_mode = 'W'
      AND ep.endpoint IN (
        '/manager/audit/scap/tailoring-file/create',
        '/manager/audit/scap/tailoring-file/edit/:id',
        '/manager/api/audit/scap/tailoring-file/create',
        '/manager/api/audit/scap/tailoring-file/update',
        '/manager/api/audit/scap/tailoring-file/delete'
      )
ON CONFLICT (endpoint_id, namespace_id) DO NOTHING;

-- =============================================================================
-- FEATURE: SCAP Content Management - Policies
-- =============================================================================

-- 1. Endpoints: Policies UI
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required) VALUES
    ('', '/manager/audit/scap/policies', 'GET', 'W', True),
    ('', '/manager/audit/scap/policy/create', 'GET', 'W', True),
    ('', '/manager/audit/scap/policy/edit/:id', 'GET', 'W', True),
    ('', '/manager/audit/scap/policy/details/:id', 'GET', 'W', True),
    ('', '/manager/api/audit/profiles/list/:type/:id', 'GET', 'W', True),
    ('', '/manager/api/audit/scap/policy/view/:id', 'GET', 'W', True),
    ('', '/manager/api/audit/scap/policy/:id/scan-history', 'GET', 'W', True),
    ('', '/manager/api/audit/scap/policy/create', 'POST', 'W', True),
    ('', '/manager/api/audit/scap/policy/update', 'POST', 'W', True),
    ('', '/manager/api/audit/scap/policy/delete', 'POST', 'W', True)
ON CONFLICT (endpoint, http_method) DO NOTHING;

-- 2. Mapping: Policies READ
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.scap.management' AND ns.access_mode = 'R'
      AND ep.endpoint IN (
        '/manager/audit/scap/policies',
        '/manager/audit/scap/policy/details/:id',
        '/manager/api/audit/profiles/list/:type/:id',
        '/manager/api/audit/scap/policy/view/:id',
        '/manager/api/audit/scap/policy/:id/scan-history'
      )
ON CONFLICT (endpoint_id, namespace_id) DO NOTHING;

-- 3. Mapping: Policies WRITE
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.scap.management' AND ns.access_mode = 'W'
      AND ep.endpoint IN (
        '/manager/audit/scap/policy/create',
        '/manager/audit/scap/policy/edit/:id',
        '/manager/api/audit/scap/policy/create',
        '/manager/api/audit/scap/policy/update',
        '/manager/api/audit/scap/policy/delete'
      )
ON CONFLICT (endpoint_id, namespace_id) DO NOTHING;


-- =============================================================================
-- FEATURE: SCAP Content Management - Final assignments to access groups
-- =============================================================================

-- Permissions: Grant to all groups
INSERT INTO access.accessGroupNamespace (group_id, namespace_id)
    SELECT ag.id, ns.id FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace = 'audit.scap.management'
ON CONFLICT (group_id, namespace_id) DO NOTHING;

-- -------------------------------------------------------------------------------

-- =============================================================================
-- FEATURE: SCAP Execution (Scanning & Remediation)
-- =============================================================================

-- 1.Unified Namespace for SCAP Execution

INSERT INTO access.namespace (namespace, access_mode, description) VALUES 
    ('audit.scap.execution', 'R', 'View SCAP scan results and remediation scripts'),
    ('audit.scap.execution', 'W', 'Schedule SCAP scans and apply remediations')
ON CONFLICT (namespace, access_mode) DO NOTHING;



-- 2. Endpoints: Scheduling & Results
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required) VALUES
    ('', '/manager/systems/details/schedule-scap-scan', 'GET', 'W', True),
    ('', '/manager/systems/ssm/audit/schedule-scap-scan', 'GET', 'W', True),
    ('', '/manager/api/audit/schedule/create', 'POST', 'W', True),
    ('', '/manager/audit/scap/scan/rule-result-details/:sid/:rrid', 'GET', 'W', True)
ON CONFLICT (endpoint, http_method) DO NOTHING;

-- 3. Endpoints: Remediation
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required) VALUES
    ('', '/manager/api/audit/scap/custom-remediation/:identifier/:benchmarkId', 'GET', 'W', True),
    ('', '/manager/api/audit/scap/custom-remediation', 'POST', 'W', True),
    ('', '/manager/api/audit/scap/custom-remediation/:identifier/:benchmarkId/:scriptType', 'DELETE', 'W', True),
    ('', '/manager/api/audit/scap/scan/rule-apply-remediation', 'POST', 'W', True)
ON CONFLICT (endpoint, http_method) DO NOTHING;

-- 4. Mapping: Execution READ (Viewing results)
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.scap.execution' AND ns.access_mode = 'R'
      AND ep.endpoint IN (
        '/manager/audit/scap/scan/rule-result-details/:sid/:rrid',
        '/manager/api/audit/scap/custom-remediation/:identifier/:benchmarkId'
      )
ON CONFLICT (endpoint_id, namespace_id) DO NOTHING;

-- 6. Mapping: Execution WRITE (Actions)
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'audit.scap.execution' AND ns.access_mode = 'W'
      AND ep.endpoint IN (
        '/manager/systems/details/schedule-scap-scan',
        '/manager/systems/ssm/audit/schedule-scap-scan',
        '/manager/api/audit/schedule/create',
        '/manager/api/audit/scap/custom-remediation',
        '/manager/api/audit/scap/custom-remediation/:identifier/:benchmarkId/:scriptType',
        '/manager/api/audit/scap/scan/rule-apply-remediation'
      )
ON CONFLICT (endpoint_id, namespace_id) DO NOTHING;

-- 6. Permissions: Grant to all groups
INSERT INTO access.accessGroupNamespace (group_id, namespace_id)
    SELECT ag.id, ns.id FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace = 'audit.scap.execution'
ON CONFLICT (group_id, namespace_id) DO NOTHING;
