 INSERT INTO rhnConfigChannelType (id, label, name, priority) 
 SELECT sequence_nextval('rhn_confchantype_id_seq'), 'state', 'Configuration channel based on SALT state', 1 from DUAL
 WHERE NOT EXISTS ( SELECT 1 FROM rhnConfigChannelType   WHERE label = 'state');

