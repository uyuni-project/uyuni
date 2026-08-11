INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/hub/scc/suma/hub_channels', 'GET', 'W', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/hub/scc/suma/hub_channels' AND http_method = 'GET');
