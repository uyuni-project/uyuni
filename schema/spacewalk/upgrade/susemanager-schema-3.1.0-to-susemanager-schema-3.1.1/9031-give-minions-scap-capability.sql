INSERT INTO rhnClientCapability(server_id, capability_name_id, version)
SELECT s.id, (SELECT id FROM rhnClientCapabilityName WHERE name='scap.xccdf_eval'), 1
FROM rhnServer s JOIN suseminioninfo mi ON s.id=mi.server_id
	LEFT JOIN rhnClientCapability c ON (c.server_id=s.id and c.capability_name_id=(SELECT id FROM rhnClientCapabilityName WHERE name='scap.xccdf_eval'))
WHERE
	c.server_id IS NULL
	;