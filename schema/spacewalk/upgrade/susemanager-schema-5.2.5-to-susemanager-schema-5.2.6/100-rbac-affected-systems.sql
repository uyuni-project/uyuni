--
-- RBAC mappings for affected systems audit endpoints
--

-- Namespaces
INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.audit.list_affected_systems', 'R', 'List visible systems with their corresponding affected packages regarding a given CVE identifier'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.audit.list_affected_systems' AND access_mode = 'R');

INSERT INTO access.namespace (namespace, access_mode, description)
    SELECT 'api.audit.list_affected_systems_by_cve', 'R', 'List known CVEs along the visible systems affected by the CVE with their corresponding affected packages'
    WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.audit.list_affected_systems_by_cve' AND access_mode = 'R');

-- Endpoints
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.audit.CVEAuditHandler.listAffectedSystems', '/manager/api/audit/listAffectedSystems', 'GET', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/audit/listAffectedSystems' AND http_method = 'GET');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT 'com.redhat.rhn.frontend.xmlrpc.audit.CVEAuditHandler.listAffectedSystemsByCve', '/manager/api/audit/listAffectedSystemsByCve', 'GET', 'A', True
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/audit/listAffectedSystemsByCve' AND http_method = 'GET');

-- Map Endpoints to Namespaces
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.audit.list_affected_systems' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/audit/listAffectedSystems' AND ep.http_method = 'GET'
    AND NOT EXISTS (
        SELECT 1 FROM access.endpointNamespace en
        WHERE en.namespace_id = ns.id AND en.endpoint_id = ep.id
    );

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.audit.list_affected_systems_by_cve' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/audit/listAffectedSystemsByCve' AND ep.http_method = 'GET'
    AND NOT EXISTS (
        SELECT 1 FROM access.endpointNamespace en
        WHERE en.namespace_id = ns.id AND en.endpoint_id = ep.id
    );

-- Permit to all access groups
INSERT INTO access.accessGroupNamespace (group_id, namespace_id)
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace IN ('api.audit.list_affected_systems', 'api.audit.list_affected_systems_by_cve')
    AND NOT EXISTS (
        SELECT 1 FROM access.accessGroupNamespace agn
        WHERE agn.group_id = ag.id AND agn.namespace_id = ns.id
    );
