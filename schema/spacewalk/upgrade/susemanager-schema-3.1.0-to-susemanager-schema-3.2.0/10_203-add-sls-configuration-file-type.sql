 INSERT INTO rhnConfigFileType(id, label, name, created, modified)
 SELECT 4, 'sls', 'SLS', current_timestamp, current_timestamp FROM DUAL
 WHERE NOT EXISTS ( SELECT 1 FROM rhnConfigFileType   WHERE label = 'sls');
