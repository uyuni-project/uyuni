-- scc endpoints

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/hub/scc/connect/organizations/systems', 'PUT', 'W', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/hub/scc/connect/organizations/systems' AND http_method = 'PUT');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/hub/scc/connect/organizations/systems/:id', 'DELETE', 'W', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/hub/scc/connect/organizations/systems/:id' AND http_method = 'DELETE');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/hub/scc/connect/organizations/virtualization_hosts', 'PUT', 'W', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/hub/scc/connect/organizations/virtualization_hosts' AND http_method = 'PUT');


