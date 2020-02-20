-- functional index for rhnISSMaster

CREATE UNIQUE INDEX rhn_server_maid_uq
  ON rhnServer
  (machine_id) where machine_id is not null;