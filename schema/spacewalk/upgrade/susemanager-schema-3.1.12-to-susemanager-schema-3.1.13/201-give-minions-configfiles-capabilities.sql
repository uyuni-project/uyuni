
SELECT lookup_client_capability('configfiles.base64_enc') FROM dual;
INSERT INTO rhnClientCapability(server_id, capability_name_id, version) SELECT s.id, (SELECT id FROM rhnClientCapabilityName 
WHERE name='configfiles.base64_enc'), 1 FROM rhnServer s, suseminioninfo mi WHERE s.id=mi.server_id AND NOT EXISTS 
(select 1 from rhnClientCapability c where c.capability_name_id=(SELECT id FROM rhnClientCapabilityName WHERE name='configfiles.base64_enc') and c.server_id=s.id);

SELECT lookup_client_capability('configfiles.deploy') FROM dual;
INSERT INTO rhnClientCapability(server_id, capability_name_id, version) SELECT s.id, (SELECT id FROM rhnClientCapabilityName 
WHERE name='configfiles.deploy'), 1 FROM rhnServer s, suseminioninfo mi WHERE s.id=mi.server_id AND NOT EXISTS 
(select 1 from rhnClientCapability c where c.capability_name_id=(SELECT id FROM rhnClientCapabilityName WHERE name='configfiles.deploy') and c.server_id=s.id);

SELECT lookup_client_capability('configfiles.mtime_upload') FROM dual;
INSERT INTO rhnClientCapability(server_id, capability_name_id, version) SELECT s.id, (SELECT id FROM rhnClientCapabilityName 
WHERE name='configfiles.mtime_upload'), 1 FROM rhnServer s, suseminioninfo mi WHERE s.id=mi.server_id AND NOT EXISTS 
(select 1 from rhnClientCapability c where c.capability_name_id=(SELECT id FROM rhnClientCapabilityName WHERE name='configfiles.mtime_upload') and c.server_id=s.id);

SELECT lookup_client_capability('configfiles.diff') FROM dual;
INSERT INTO rhnClientCapability(server_id, capability_name_id, version) SELECT s.id, (SELECT id FROM rhnClientCapabilityName 
WHERE name='configfiles.diff'), 1 FROM rhnServer s, suseminioninfo mi WHERE s.id=mi.server_id AND NOT EXISTS 
(select 1 from rhnClientCapability c where c.capability_name_id=(SELECT id FROM rhnClientCapabilityName WHERE name='configfiles.diff') and c.server_id=s.id);

SELECT lookup_client_capability('configfiles.upload') FROM dual;
INSERT INTO rhnClientCapability(server_id, capability_name_id, version) SELECT s.id, (SELECT id FROM rhnClientCapabilityName 
WHERE name='configfiles.upload'), 1 FROM rhnServer s, suseminioninfo mi WHERE s.id=mi.server_id AND NOT EXISTS 
(select 1 from rhnClientCapability c where c.capability_name_id=(SELECT id FROM rhnClientCapabilityName WHERE name='configfiles.upload') and c.server_id=s.id);
