INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('aarch64-redhat-linux'),
    lookup_sg_type('ansible_control_node')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('aarch64-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_control_node')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('ppc64le-redhat-linux'),
    lookup_sg_type('ansible_control_node')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('ppc64le-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_control_node')
);

INSERT INTO rhnServerServerGroupArchCompat ( server_arch_id, server_group_type )
SELECT
    lookup_server_arch('s390x-redhat-linux'),
    lookup_sg_type('ansible_control_node')
WHERE NOT EXISTS (
    SELECT 1
    FROM rhnServerServerGroupArchCompat
    WHERE server_arch_id = lookup_server_arch('s390x-redhat-linux')
        AND server_group_type = lookup_sg_type('ansible_control_node')
);

