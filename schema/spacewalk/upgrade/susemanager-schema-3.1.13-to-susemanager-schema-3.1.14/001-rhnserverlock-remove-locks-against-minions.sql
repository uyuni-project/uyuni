DELETE FROM rhnserverlock sl
	WHERE sl.server_id IN (SELECT sm.server_id FROM suseminioninfo sm);

