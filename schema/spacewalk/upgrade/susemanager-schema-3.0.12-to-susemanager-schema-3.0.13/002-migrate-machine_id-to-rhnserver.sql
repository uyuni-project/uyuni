alter table rhnServer add column machine_id VARCHAR2(256);

update rhnServer s set machine_id=(select machine_id from suseMinionInfo m where m.server_id=s.id);

alter table suseMinionInfo drop constraint if exists rhn_minion_info_miid_uq;
alter table suseMinionInfo drop column machine_id;