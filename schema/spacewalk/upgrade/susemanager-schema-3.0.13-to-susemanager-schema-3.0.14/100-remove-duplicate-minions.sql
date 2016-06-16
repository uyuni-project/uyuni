DELETE FROM 
	rhnservernetwork 
	WHERE id IN 
	(
		SELECT DISTINCT(a.id)
		FROM rhnservernetwork a, rhnservernetwork b, suseminioninfo c
		WHERE
			1=1
			AND a.server_id = b.server_id
			AND a.id < b.id
			AND a.server_id = c.server_id
	)
;
