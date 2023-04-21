UPDATE rhnChannel
   SET gpg_key_url = 'file:///usr/lib/rpm/gnupg/keys/gpg-pubkey-3dbdc284-53674dd4.asc'
 WHERE gpg_key_id = '3DBDC284'
   AND label NOT LIKE '%micro%';
-- opensuse micro may not have openSUSE-build-key package installed

UPDATE rhnChannel
   SET gpg_key_url = 'file:///usr/lib/rpm/gnupg/keys/gpg-pubkey-29b700a4-62b07e22.asc',
       gpg_key_id = '29B700A4',
       gpg_key_fp = 'AD48 5664 E901 B867 051A B15F 35A2 F86E 29B7 00A4'
 WHERE gpg_key_id = '3DBDC284'
   AND (label like 'opensuse_leap15_5%'
        OR label like 'opensuse_tumbleweed%');
