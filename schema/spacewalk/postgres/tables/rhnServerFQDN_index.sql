-- function index for rhnServerFQDN

CREATE UNIQUE INDEX rhn_srv_fqdn_prim_fqdn
  ON rhnServerFQDN
  (server_id) where is_primary = 'Y';
