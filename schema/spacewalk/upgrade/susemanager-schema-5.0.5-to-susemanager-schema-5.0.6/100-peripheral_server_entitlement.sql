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
    'peripheral_server',
    'Peripheral Server',
    'N',
    'N'
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerGroupType
    WHERE label = 'peripheral_server'
);


--------------------------------------------------------------------------------
-- rhnSGTypeBaseAddonCompat ----------------------------------------------------
--------------------------------------------------------------------------------
INSERT INTO rhnSGTypeBaseAddonCompat(
    base_id,
    addon_id)
SELECT
    lookup_sg_type('salt_entitled'),
    lookup_sg_type('peripheral_server')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnSGTypeBaseAddonCompat
    WHERE base_id = lookup_sg_type('salt_entitled')
        AND addon_id = lookup_sg_type('peripheral_server')
);

INSERT INTO rhnSGTypeBaseAddonCompat(
    base_id,
    addon_id)
SELECT
    lookup_sg_type('foreign_entitled'),
    lookup_sg_type('peripheral_server')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnSGTypeBaseAddonCompat
    WHERE base_id = lookup_sg_type('foreign_entitled')
        AND addon_id = lookup_sg_type('peripheral_server')
);


--------------------------------------------------------------------------------
-- rhnServerServerGroupArchCompat ----------------------------------------------
--------------------------------------------------------------------------------
INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('x86_64-redhat-linux'),
    lookup_sg_type('peripheral_server')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('x86_64-redhat-linux')
        AND server_group_type = lookup_sg_type('peripheral_server')
);


INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('amd64-redhat-linux'),
    lookup_sg_type('peripheral_server')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('amd64-redhat-linux')
        AND server_group_type = lookup_sg_type('peripheral_server')
);

--------------------------------------------------------------------------------
-- existing server groups update -----------------------------------------------
--------------------------------------------------------------------------------
INSERT INTO rhnServerGroup ( id, name, description, group_type, org_id )
SELECT nextval('rhn_server_group_id_seq'), sgt.name, sgt.name, sgt.id, org.id
FROM rhnServerGroupType sgt, web_customer org
WHERE sgt.label = 'peripheral_server' AND org.id NOT IN (
    SELECT sg.org_id from rhnServerGroup sg
    WHERE sg.name = 'Peripheral Server'
);
