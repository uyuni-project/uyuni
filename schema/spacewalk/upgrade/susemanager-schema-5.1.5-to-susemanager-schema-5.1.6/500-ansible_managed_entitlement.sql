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
    'ansible_managed',
    'Ansible Managed Servers',
    'N',
    'N'
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerGroupType
    WHERE label = 'ansible_managed'
);


--------------------------------------------------------------------------------
-- rhnSGTypeBaseAddonCompat ----------------------------------------------------
--------------------------------------------------------------------------------
INSERT INTO rhnSGTypeBaseAddonCompat(
    base_id,
    addon_id)
SELECT
    lookup_sg_type('salt_entitled'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnSGTypeBaseAddonCompat
    WHERE base_id = lookup_sg_type('salt_entitled')
        AND addon_id = lookup_sg_type('ansible_managed')
);


--------------------------------------------------------------------------------
-- rhnServerServerGroupArchCompat ----------------------------------------------
--------------------------------------------------------------------------------
INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('i386-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('i386-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('i386-debian-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('i386-debian-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('i486-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('i486-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('i586-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('i586-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('i686-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('i686-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('athlon-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('athlon-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('alpha-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('alpha-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('alpha-debian-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('alpha-debian-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('alphaev6-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('alphaev6-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('ia64-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('ia64-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('ia64-debian-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('ia64-debian-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('sparc-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('sparc-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('sparc-debian-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('sparc-debian-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('sparcv9-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('sparcv9-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('sparc64-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('sparc64-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('s390-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('s390-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('aarch64-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('aarch64-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('armv7l-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('armv7l-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('armv5tejl-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('armv5tejl-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('armv6l-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('armv6l-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('armv6hl-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('armv6hl-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('s390-debian-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('s390-debian-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('s390x-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('s390x-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('ppc-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('ppc-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('powerpc-debian-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('powerpc-debian-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('ppc64-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('ppc64-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('ppc64le-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('ppc64le-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('pSeries-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('pSeries-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('iSeries-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('iSeries-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('x86_64-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('x86_64-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('ia32e-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('ia32e-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('amd64-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('amd64-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('amd64-debian-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('amd64-debian-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('arm64-debian-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('arm64-debian-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('ppc64iseries-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('ppc64iseries-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('ppc64pseries-redhat-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('ppc64pseries-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('arm-debian-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('arm-debian-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('armv6l-debian-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('armv6l-debian-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('mips-debian-linux'),
    lookup_sg_type('ansible_managed')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('mips-debian-linux')
        AND server_group_type = lookup_sg_type('ansible_managed')
);


--------------------------------------------------------------------------------
-- existing server groups update -----------------------------------------------
--------------------------------------------------------------------------------
INSERT INTO rhnServerGroup ( id, name, description, group_type, org_id )
SELECT nextval('rhn_server_group_id_seq'), sgt.name, sgt.name, sgt.id, org.id
FROM rhnServerGroupType sgt, web_customer org
WHERE sgt.label = 'ansible_managed' AND org.id NOT IN (
    SELECT sg.org_id from rhnServerGroup sg
    WHERE sg.name = 'Ansible Managed Servers'
);
