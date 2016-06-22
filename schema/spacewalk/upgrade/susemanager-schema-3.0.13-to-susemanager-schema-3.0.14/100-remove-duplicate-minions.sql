DELETE FROM 
  rhnServerNetwork
    WHERE id IN
    (
        SELECT DISTINCT(a.id)
        FROM rhnServerNetwork a, rhnServerNetwork b, suseMinionInfo c
        WHERE
            a.server_id = b.server_id
            AND a.id < b.id
            AND a.server_id = c.server_id
    )
;
