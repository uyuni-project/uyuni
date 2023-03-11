
alter table rhnServerNetInterface add id numeric;

create sequence rhn_srv_net_iface_id_seq;

alter table rhnServerNetInterface disable trigger rhn_srv_net_iface_mod_trig;
update rhnServerNetInterface set id = nextval('rhn_srv_net_iface_id_seq');
alter table rhnServerNetInterface enable trigger rhn_srv_net_iface_mod_trig;

alter table rhnServerNetInterface alter column id set not null;

alter table rhnServerNetInterface add constraint rhn_srv_net_iface_id_pk primary key ( id );
