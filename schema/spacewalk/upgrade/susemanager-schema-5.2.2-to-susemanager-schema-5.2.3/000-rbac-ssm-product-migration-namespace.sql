UPDATE access.endpointnamespace
   SET namespace_id = (SELECT id FROM access.namespace WHERE namespace = 'systems.software.migration')
 WHERE namespace_id = (SELECT id FROM access.namespace WHERE namespace = 'systems.migration')
;

DELETE
  FROM access.namespace
 WHERE namespace = 'systems.migration'
;
