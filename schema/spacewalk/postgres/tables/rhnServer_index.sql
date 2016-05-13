-- oracle equivalent source sha1 c8b1eab237a121b2b17ce2fa95dc6818af7ba60f
-- functional index for rhnISSMaster

CREATE UNIQUE INDEX rhn_server_maid_uq
  ON rhnServer
  (machine_id) where machine_id is not null;