UPDATE rhnChannel
   SET gpg_key_url = 'file:///usr/lib/rpm/gnupg/keys/gpg-pubkey-3dbdc284-53674dd4.asc'
 WHERE gpg_key_id = '3DBDC284'
   AND label NOT LIKE '%micro%';
-- opensuse micro may not have openSUSE-build-key package installed
