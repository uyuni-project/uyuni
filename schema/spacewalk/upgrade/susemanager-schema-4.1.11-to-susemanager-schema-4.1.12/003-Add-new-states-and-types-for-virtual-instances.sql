INSERT INTO rhnVirtualInstanceState (id, name, label) SELECT sequence_nextval('rhn_vis_id_seq'), 'Powering On', 'powering_on' from dual WHERE NOT EXISTS (SELECT 1 FROM rhnVirtualInstanceState WHERE label = 'powering_on');

INSERT INTO rhnVirtualInstanceState (id, name, label) SELECT sequence_nextval('rhn_vis_id_seq'), 'Shutting Down', 'shutting_down' from dual WHERE NOT EXISTS (SELECT 1 FROM rhnVirtualInstanceState WHERE label = 'shutting_down');

INSERT INTO rhnVirtualInstanceState (id, name, label) SELECT sequence_nextval('rhn_vis_id_seq'), 'Powering Off', 'powering_off' from dual WHERE NOT EXISTS (SELECT 1 FROM rhnVirtualInstanceState WHERE label = 'powering_off');

INSERT INTO rhnVirtualInstanceState (id, name, label) SELECT sequence_nextval('rhn_vis_id_seq'), 'Pausing', 'pausing' from dual WHERE NOT EXISTS (SELECT 1 FROM rhnVirtualInstanceState WHERE label = 'pausing');

INSERT INTO rhnVirtualInstanceState (id, name, label) SELECT sequence_nextval('rhn_vis_id_seq'), 'Suspending', 'suspending' from dual WHERE NOT EXISTS (SELECT 1 FROM rhnVirtualInstanceState WHERE label = 'suspending');

INSERT INTO rhnVirtualInstanceState (id, name, label) SELECT sequence_nextval('rhn_vis_id_seq'), 'Suspended', 'suspended' from dual WHERE NOT EXISTS (SELECT 1 FROM rhnVirtualInstanceState WHERE label = 'suspended');

INSERT INTO rhnVirtualInstanceState (id, name, label) SELECT sequence_nextval('rhn_vis_id_seq'), 'Resuming', 'resuming' from dual WHERE NOT EXISTS (SELECT 1 FROM rhnVirtualInstanceState WHERE label = 'resuming');

INSERT INTO rhnVirtualInstanceState (id, name, label) SELECT sequence_nextval('rhn_vis_id_seq'), 'Resetting', 'resetting' from dual WHERE NOT EXISTS (SELECT 1 FROM rhnVirtualInstanceState WHERE label = 'resetting');

INSERT INTO rhnVirtualInstanceState (id, name, label) SELECT sequence_nextval('rhn_vis_id_seq'), 'Migrating', 'migrating' from dual WHERE NOT EXISTS (SELECT 1 FROM rhnVirtualInstanceState WHERE label = 'migrating');

INSERT INTO rhnVirtualInstanceType (id, name, label)
SELECT sequence_nextval('rhn_vit_id_seq'), 'Nutanix AHV', 'nutanix'
WHERE NOT EXISTS ( SELECT 1 FROM rhnVirtualInstanceType WHERE label = 'nutanix' AND name = 'Nutanix AHV');
