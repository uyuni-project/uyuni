INSERT INTO rhnVirtualInstanceType (id, name, label)
  SELECT sequence_nextval('rhn_vit_id_seq'), 'VirtualPC', 'virtualpc'
  FROM dual
  WHERE NOT EXISTS(select 1 from rhnVirtualInstanceType WHERE label='virtualpc');
