--- After CentOS EoL, it got removed from the mirrorlist and moved to the vault
UPDATE rhnContentSource
SET source_url = concat('https://vault.centos.org/centos/6/', array_to_string(regexp_match(source_url, 'repo=(.+)'), ''), '/' ,array_to_string(regexp_match(source_url, 'arch=(.+)&'), ''), '/')
WHERE source_url LIKE 'http://mirrorlist.centos.org/?release=6%' AND org_id IS NOT NULL;
