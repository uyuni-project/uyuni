INSERT INTO rhnVirtualInstanceType (id, name, label)
SELECT sequence_nextval('rhn_vit_id_seq'), 'Amazon EC2/KVM', 'aws_kvm'
WHERE NOT EXISTS ( SELECT 1 FROM rhnVirtualInstanceType WHERE label = 'aws_kvm' OR label = 'aws_nitro');

INSERT INTO rhnVirtualInstanceType (id, name, label)
SELECT sequence_nextval('rhn_vit_id_seq'), 'Amazon EC2/Xen', 'aws_xen'
WHERE NOT EXISTS ( SELECT 1 FROM rhnVirtualInstanceType WHERE label = 'aws_xen');
