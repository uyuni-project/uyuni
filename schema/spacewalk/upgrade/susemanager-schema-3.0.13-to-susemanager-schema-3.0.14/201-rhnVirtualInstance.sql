-- oracle equivalent source sha1 48b244c90a55fbc27436e7d8c2b1ba7485649009

delete from rhnVirtualInstance where virtual_system_id is NULL and host_system_id is not NULL and uuid in (select uuid from rhnVirtualInstance group by uuid having count(uuid) > 1);
delete from rhnVirtualInstance where virtual_system_id is NULL and host_system_id is NULL and uuid is not NULL;

DROP TRIGGER IF EXISTS rhn_virtinst_del_trig ON rhnvirtualinstance;
DROP FUNCTION IF EXISTS rhn_virtinst_del_trig_fun();

