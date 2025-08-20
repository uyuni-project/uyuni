--------------------------------------------------------------------------------
-- Ensure existing proxies get the proxy_entitled and update the group count ---
--------------------------------------------------------------------------------
INSERT INTO rhnservergroupmembers (server_id, server_group_id)
SELECT rpi.server_id, sg.id
FROM rhnproxyinfo rpi
JOIN rhnServer s ON rpi.server_id = s.id
JOIN rhnservergroup sg ON sg.name = 'Proxy'
WHERE
    s.org_id = sg.org_id
    AND NOT EXISTS (
        SELECT 1
        FROM rhnservergroupmembers sgm
        WHERE sgm.server_id = rpi.server_id
          AND sgm.server_group_id = sg.id
    );

UPDATE rhnservergroup sg
SET current_members = (
    SELECT COUNT(*)
    FROM rhnservergroupmembers sgm
    WHERE sgm.server_group_id = sg.id
)
WHERE sg.name = 'Proxy';
