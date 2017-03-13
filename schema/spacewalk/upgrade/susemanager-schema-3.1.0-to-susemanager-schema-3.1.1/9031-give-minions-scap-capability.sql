INSERT INTO rhnClientCapability(server_id, capability_name_id, version) SELECT s.id, (SELECT id FROM rhnClientCapabilityName WHERE name='scap.xccdf_eval'), 1 FROM rhnServer s, suseminioninfo mi WHERE s.id=mi.server_id AND NOT EXISTS (select 1 from rhnClientCapability c where c.capability_name_id=(SELECT id FROM rhnClientCapabilityName WHERE name='scap.xccdf_eval') and c.server_id=s.id);

