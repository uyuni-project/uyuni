--------------------------------------------------------------------------------
-- Ensure existing proxies get the proxy_entitled and update the group count ---
--------------------------------------------------------------------------------
INSERT INTO rhnservergroupmembers (server_id, server_group_id)
    SELECT rpi.server_id, (SELECT id FROM rhnservergroup WHERE name = 'Proxy')
    FROM rhnproxyinfo rpi
    WHERE NOT EXISTS (
        SELECT 1
        FROM rhnservergroupmembers sg
        WHERE sg.server_id = rpi.server_id
          AND sg.server_group_id = (SELECT id FROM rhnservergroup WHERE name = 'Proxy')
    );

UPDATE rhnservergroup
    SET current_members = (
        SELECT COUNT(*)
        FROM rhnservergroupmembers
        WHERE server_group_id = (SELECT id FROM rhnservergroup WHERE name = 'Proxy')
    )
    WHERE id = (SELECT id FROM rhnservergroup WHERE name = 'Proxy');
