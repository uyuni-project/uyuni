INSERT INTO rhnCpuArch (id, label, name)
SELECT sequence_nextval('rhn_cpu_arch_id_seq'), 'cloud', 'cloud'
WHERE NOT EXISTS ( SELECT 1 FROM rhnCpuArch WHERE label = 'cloud' AND name = 'cloud');

INSERT INTO rhnServerArch (id, label, name, arch_type_id)
SELECT sequence_nextval('rhn_server_arch_id_seq'), 'cloud', 'cloud', lookup_arch_type('rpm')
WHERE NOT EXISTS ( SELECT 1 FROM rhnServerArch WHERE label = 'cloud' AND name = 'cloud');

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT lookup_server_arch('cloud'), lookup_sg_type('foreign_entitled')
WHERE NOT EXISTS ( SELECT 1 FROM rhnServerServerGroupArchCompat 
	WHERE server_arch_id = lookup_server_arch('cloud') AND server_group_type = lookup_sg_type('foreign_entitled'));

INSERT INTO rhnVirtualInstanceType (id, name, label)
SELECT sequence_nextval('rhn_vit_id_seq'), 'Azure', 'azure'
WHERE NOT EXISTS ( SELECT 1 FROM rhnVirtualInstanceType WHERE label = 'azure' AND name = 'Azure');

INSERT INTO rhnVirtualInstanceType (id, name, label)
SELECT sequence_nextval('rhn_vit_id_seq'), 'Amazon EC2', 'aws'
WHERE NOT EXISTS ( SELECT 1 FROM rhnVirtualInstanceType WHERE label = 'aws' AND name = 'Amazon EC2');

INSERT INTO rhnVirtualInstanceType (id, name, label)
SELECT sequence_nextval('rhn_vit_id_seq'), 'Google CE', 'gce'
WHERE NOT EXISTS ( SELECT 1 FROM rhnVirtualInstanceType WHERE label = 'gce' AND name = 'Google CE');
