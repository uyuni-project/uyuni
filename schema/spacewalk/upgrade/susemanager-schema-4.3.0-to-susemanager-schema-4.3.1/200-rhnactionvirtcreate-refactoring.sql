DO $$
    BEGIN
        IF EXISTS
            (
                SELECT 1
                FROM information_schema.columns
                WHERE table_name='rhnactionvirtcreate' AND column_name='vm_type'
            )
        THEN
            -- Create the details colument
            ALTER TABLE rhnactionvirtcreate ADD COLUMN IF NOT EXISTS details TEXT;

            -- Convert all data into the details field
            UPDATE rhnactionvirtcreate
            SET details = action_details.json
            FROM
            (
                SELECT a.action_id AS id,
                   CONCAT(
                     '{',
                       CASE WHEN a.uuid IS NOT NULL THEN CONCAT('"uuid": "', a.uuid, '", ') END,
                       '"type": "', a.vm_type, '", ',
                       '"name": "', a.vm_name, '", ',
                       '"osType": "', a.os_type, '", ',
                       '"memory": ', a.memory, ', ',
                       '"vcpu": ', a.vcpus, ', ',
                       '"arch": "', a.arch, '", ',
                       CASE WHEN a.graphics_type IS NOT NULL THEN CONCAT('"graphicsType": "', a.graphics_type, '", ') END,
                       CASE WHEN a.cobbler_system IS NOT NULL THEN CONCAT('"cobbler_system": "', a.cobbler_system, '", ') END,
                       CASE WHEN a.kickstart_host IS NOT NULL THEN CONCAT('"kickstart_host": "', a.kickstart_host , '", ') END,
                       CASE WHEN a.kernel_options IS NOT NULL THEN CONCAT('"kernel_options": "', a.kernel_options , '", ') END,
                       CASE WHEN a.cluster_definitions IS NOT NULL THEN CONCAT('"cluster_definitions": "', a.cluster_definitions, '", ') END,
                       CASE WHEN string_agg(d.json, '') IS NOT NULL THEN CONCAT('"disks": [', string_agg(d.json, ''), '], ') END,
                       CASE WHEN string_agg(i.json, '') IS NOT NULL THEN CONCAT('"interfaces": [', string_agg(i.json, ''), '], ') END,
                       '"remove_disks": ', CASE WHEN a.remove_disks = 'Y' THEN 'true' ELSE 'false' END, ', ',
                       '"remove_interfaces": ', CASE WHEN a.remove_interfaces = 'Y' THEN 'true' ELSE 'false' END,
                     '}'
                   ) AS json
                FROM rhnactionvirtcreate a
                LEFT JOIN (
                  SELECT action_id, CONCAT(
                    '{',
                        CASE WHEN bus IS NOT NULL THEN CONCAT('"bus": "', bus, '", ') END,
                        CASE WHEN format IS NOT NULL THEN CONCAT('"format": "', format, '", ') END,
                        CASE WHEN pool IS NOT NULL THEN CONCAT('"pool": "', pool, '", ') END,
                        CASE WHEN source_file IS NOT NULL THEN CONCAT('"source_file": "', source_file, '",') END,
                        '"size": ', size, ', '
                        '"device": "', device, '"'
                    '}'
                  ) AS json
                  FROM rhnactionvirtcreatediskdetails
                ) AS d ON a.action_id = d.action_id
                LEFT JOIN (
                  SELECT action_id, CONCAT(
                    '{',
                      CASE WHEN mac IS NOT NULL THEN CONCAT('"mac": "', mac, '", ') END,
                      '"type": "', type, '", '
                      '"source": "', source, '"'
                    '}'
                  ) AS json
                  FROM rhnactionvirtcreateinterfacedetails
                ) AS i ON a.action_id = i.action_id
                GROUP BY a.action_id
            ) AS action_details
            WHERE action_id = action_details.id;

            -- Remove the now useless columns and tables
            ALTER TABLE rhnactionvirtcreate
                DROP COLUMN IF EXISTS vm_type CASCADE,
                DROP COLUMN IF EXISTS vm_name CASCADE,
                DROP COLUMN IF EXISTS os_type CASCADE,
                DROP COLUMN IF EXISTS memory,
                DROP COLUMN IF EXISTS vcpus,
                DROP COLUMN IF EXISTS arch CASCADE,
                DROP COLUMN IF EXISTS graphics_type CASCADE,
                DROP COLUMN IF EXISTS remove_disks,
                DROP COLUMN IF EXISTS remove_interfaces,
                DROP COLUMN IF EXISTS cobbler_system CASCADE,
                DROP COLUMN IF EXISTS kickstart_host CASCADE,
                DROP COLUMN IF EXISTS kernel_options CASCADE,
                DROP COLUMN IF EXISTS cluster_definitions;
            DROP TABLE IF EXISTS rhnactionvirtcreatediskdetails, rhnactionvirtcreateinterfacedetails;
            DROP INDEX IF EXISTS rhn_action_virt_create_disk_details_id_idx, rhn_action_virt_create_iface_details_id_idx;
            DROP SEQUENCE IF EXISTS rhn_action_virt_create_disk_details_id_seq, rhn_action_virt_create_iface_details_id_seq;
        END IF;
    END
$$ LANGUAGE plpgsql;
