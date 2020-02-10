-- oracle equivalent source sha1 35bef26529b65c45ad436623620d99a7cf6ba2c2
-- function index for rhnServer
alter table rhnServer add column machine_id VARCHAR(256);

CREATE UNIQUE INDEX rhn_server_maid_uq
  ON rhnServer
  (machine_id) where machine_id is not null;

update rhnServer s set machine_id=(select machine_id from suseMinionInfo m where m.server_id=s.id);

alter table suseMinionInfo drop constraint if exists rhn_minion_info_miid_uq;
alter table suseMinionInfo drop column machine_id;
