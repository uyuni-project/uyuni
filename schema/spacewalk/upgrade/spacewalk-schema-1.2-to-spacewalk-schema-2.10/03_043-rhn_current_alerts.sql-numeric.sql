alter table rhn_current_alerts alter column recid type numeric(12);
alter table rhn_current_alerts alter column original_server type numeric(12);
alter table rhn_current_alerts alter column current_server type numeric(12);
alter table rhn_current_alerts alter column escalation_level type numeric(2);
alter table rhn_current_alerts alter column host_probe_id type numeric(12);
alter table rhn_current_alerts alter column service_probe_id type numeric(12);
alter table rhn_current_alerts alter column customer_id type numeric(12);
alter table rhn_current_alerts alter column netsaint_id type numeric(12);
