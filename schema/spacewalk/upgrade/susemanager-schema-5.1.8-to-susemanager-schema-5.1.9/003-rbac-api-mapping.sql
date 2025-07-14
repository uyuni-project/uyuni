INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
  SELECT 'com.redhat.rhn.frontend.xmlrpc.system.SystemHandler.scheduleSupportDataUpload', '/manager/api/system/scheduleSupportDataUpload', 'POST', 'A', True
   WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/api/system/scheduleSupportDataUpload' AND http_method = 'POST');

INSERT INTO access.namespace (namespace, access_mode, description)
 SELECT 'api.system.schedule_support_data_upload', 'W', 'Schedule fetch and upload support data from a system to SCC'
  WHERE NOT EXISTS (SELECT 1 FROM access.namespace WHERE namespace = 'api.system.schedule_support_data_upload' AND access_mode = 'W');

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
   SELECT ns.id, ep.id
     FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'api.system.schedule_support_data_upload' AND ns.access_mode = 'W'
      AND ep.endpoint = '/manager/api/system/scheduleSupportDataUpload' AND ep.http_method = 'POST'
    ON CONFLICT DO NOTHING;

-- Namespace: system
-- generic also added via common/data/accessGroupNamespace.sql
INSERT INTO access.accessGroupNamespace
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace ~ '^api\.system\.[^.]*$'
    AND ns.namespace NOT IN (
        'api.system.get_pillar',
        'api.system.set_pillar',
        'api.system.set_group_membership'
    )
    ON CONFLICT DO NOTHING;
