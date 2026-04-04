DELETE FROM access.endpointNamespace
WHERE endpoint_id IN (SELECT id FROM access.endpoint WHERE endpoint = '/manager/api/maintenance/schedule/:id/assign' AND http_method = 'PSOT');

DELETE FROM access.endpoint
WHERE endpoint = '/manager/api/maintenance/schedule/:id/assign' AND http_method = 'PSOT';
