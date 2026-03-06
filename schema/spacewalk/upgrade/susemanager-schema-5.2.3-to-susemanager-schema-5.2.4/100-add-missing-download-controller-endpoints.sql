

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/download/hubsync/:sccrepoid/:channel/getPackage/:file', 'GET', 'W', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/download/hubsync/:sccrepoid/:channel/getPackage/:file' AND http_method = 'GET');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/download/hubsync/:sccrepoid/:channel/getPackage/:org/:checksum/:file', 'GET', 'W', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/download/hubsync/:sccrepoid/:channel/getPackage/:org/:checksum/:file' AND http_method = 'GET');

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/download/hubsync/:sccrepoid/:channel/getPackage/:file', 'HEAD', 'W', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/download/hubsync/:sccrepoid/:channel/getPackage/:file' AND http_method = 'HEAD');
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    SELECT '', '/manager/download/hubsync/:sccrepoid/:channel/getPackage/:org/:checksum/:file', 'HEAD', 'W', False
    WHERE NOT EXISTS (SELECT 1 FROM access.endpoint WHERE endpoint = '/manager/download/hubsync/:sccrepoid/:channel/getPackage/:org/:checksum/:file' AND http_method = 'HEAD');



