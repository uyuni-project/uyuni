DELETE FROM access.endpointNamespace
WHERE endpoint_id IN (SELECT id FROM access.endpoint WHERE endpoint = '/manager/api/system/registerPeripheralServer');

DELETE FROM access.endpoint
WHERE endpoint = '/manager/api/system/registerPeripheralServer';
