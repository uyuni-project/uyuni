--- After CentOS8 EoL, we need to use the EL8 client tools for all EL8 clones
UPDATE rhnContentSource
SET source_url = 'https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master:/EL8-Uyuni-Client-Tools/EL_8/'
WHERE source_url = 'https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Master:/CentOS8-Uyuni-Client-Tools/CentOS_8/' AND org_id IS NOT NULL;
UPDATE rhnContentSource
SET source_url = 'https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Stable:/EL8-Uyuni-Client-Tools/EL_8/'
WHERE source_url = 'https://download.opensuse.org/repositories/systemsmanagement:/Uyuni:/Stable:/CentOS8-Uyuni-Client-Tools/CentOS_8/' AND org_id IS NOT NULL;
