--------------------------------------------------------------------------------
-- rhnServerGroupType ----------------------------------------------------------
--------------------------------------------------------------------------------
INSERT INTO rhnServerGroupType(
    id,
    label,
    name,
    permanent,
    is_base)
SELECT
    sequence_nextval('rhn_servergroup_type_seq'),
    'proxy_entitled',
    'Proxy',
    'N',
    'N'
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerGroupType
    WHERE label = 'proxy_entitled'
);

--------------------------------------------------------------------------------
-- rhnServerGroup --------------------------------------------------------------
--------------------------------------------------------------------------------
INSERT INTO rhnServerGroup ( id, name, description, group_type, org_id )
SELECT nextval('rhn_server_group_id_seq'), sgt.name, sgt.name, sgt.id, org.id
FROM rhnServerGroupType sgt, web_customer org
WHERE sgt.label = 'proxy_entitled' AND org.id NOT IN (
    SELECT sg.org_id from rhnServerGroup sg
    WHERE sg.name = 'Proxy'
);


--------------------------------------------------------------------------------
-- rhnSGTypeBaseAddonCompat ----------------------------------------------------
--------------------------------------------------------------------------------
INSERT INTO rhnSGTypeBaseAddonCompat(base_id, addon_id)
SELECT
    lookup_sg_type('salt_entitled'),
    lookup_sg_type('proxy_entitled')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnSGTypeBaseAddonCompat
    WHERE base_id = lookup_sg_type('salt_entitled')
        AND addon_id = lookup_sg_type('proxy_entitled')
);

INSERT INTO rhnSGTypeBaseAddonCompat(base_id, addon_id)
SELECT
    lookup_sg_type('foreign_entitled'),
    lookup_sg_type('proxy_entitled')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnSGTypeBaseAddonCompat
    WHERE base_id = lookup_sg_type('foreign_entitled')
        AND addon_id = lookup_sg_type('proxy_entitled')
);


--------------------------------------------------------------------------------
-- rhnServerServerGroupArchCompat ----------------------------------------------
--------------------------------------------------------------------------------
DO $$
DECLARE
    loop_server_arch_id INT; 
    proxy_sg_type_id INT;
BEGIN
    proxy_sg_type_id := lookup_sg_type('proxy_entitled');
    
    FOR loop_server_arch_id IN
        SELECT id FROM rhnserverarch
    LOOP
        INSERT INTO rhnServerServerGroupArchCompat (server_arch_id, server_group_type)
        SELECT
            loop_server_arch_id, 
            proxy_sg_type_id 
        WHERE NOT EXISTS (
            SELECT 1
            FROM rhnServerServerGroupArchCompat AS rs
            WHERE rs.server_arch_id = loop_server_arch_id  AND rs.server_group_type = proxy_sg_type_id
        );
    END LOOP;
END $$;