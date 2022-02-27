--- After CentOS8 EoL, we need to use the EL8 client tools for all EL8 clones
CREATE OR REPLACE PROCEDURE public.el8_ctools_for_all (v_source_url_el8 VARCHAR, v_source_url_centos8 VARCHAR, v_repo_label_regex VARCHAR, v_channel_label_regex VARCHAR)
AS $$
BEGIN
    -- If we have CentOS8 or EL8 client tools channels, we may need to create the EL8 repository
    IF EXISTS (SELECT id FROM rhnchannel WHERE label ~ v_channel_label_regex) THEN
        -- If the EL8 repo does not exist, create one, using the existing CentOS repo as template
        -- as it will be the one in use
        RAISE NOTICE USING MESSAGE = 'There are EL/CentOS client tools channels';
        IF NOT EXISTS (SELECT id FROM rhnContentSource WHERE source_url=v_source_url_el8) THEN
            RAISE NOTICE USING MESSAGE = 'EL8 client tools repo does not exist, creating it';
            INSERT INTO rhnContentSource (
               id,
               org_id,
               type_id,
               source_url,
               label,
               metadata_signed)
            VALUES (
               nextval('rhn_chan_content_src_id_seq'),
               (SELECT org_id FROM rhnContentSource WHERE source_url=v_source_url_centos8),
               500,
               v_source_url_el8,
               REPLACE((SELECT label FROM rhnContentSource WHERE source_url=v_source_url_centos8), 'CentOS', 'AlmaLinux'),
               'N');
        END IF;
        -- Make sure CentOS8 and EL8 client tools channels are all using the EL8 repository
        RAISE NOTICE USING MESSAGE = 'Adjusting client tools channels to use EL client tools repo';
        UPDATE rhnChannelContentSource SET source_id=(SELECT id FROM rhnContentSource WHERE source_url=v_source_url_el8)
        WHERE source_id IN (SELECT id FROM rhnContentSource WHERE rhnContentSource.label ~ v_repo_label_regex);
    END IF;
    COMMIT;
END;
$$
LANGUAGE plpgsql;

-- Stable
CALL public.el8_ctools_for_all(
  'https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Stable:/EL8-Uyuni-Client-Tools/EL_8/',
  'https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Stable:/CentOS8-Uyuni-Client-Tools/CentOS_8/',
  E'^External - Uyuni Client Tools for (AlmaLinux|CentOS|Rocky Linux|Oracle Linux) 8 \\([A-Za-z0-9_]+\\)$',
  '^(almalinux|centos|rockylinux|oraclelinux)8-uyuni-client-[A-Za-z0-9_]+$'
);

-- Master
CALL public.el8_ctools_for_all(
  'https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master:/EL8-Uyuni-Client-Tools/EL_8/',
  'https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master:/CentOS8-Uyuni-Client-Tools/CentOS_8/',
  E'^External - Uyuni Client Tools for (AlmaLinux|CentOS|Rocky Linux|Oracle Linux) 8 \\([A-Za-z0-9_]+\\) \\(Development\\)$',
  '^(almalinux|centos|rockylinux|oraclelinux)8-uyuni-client-devel-[A-Za-z0-9_]+$'
);

DROP PROCEDURE public.el8_ctools_for_all;
