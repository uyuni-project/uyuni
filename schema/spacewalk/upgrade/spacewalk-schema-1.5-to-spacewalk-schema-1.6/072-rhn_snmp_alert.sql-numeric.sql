alter table rhn_snmp_alert alter column recid type numeric(12);
alter table rhn_snmp_alert alter column sender_cluster_id type numeric(12);
alter table rhn_snmp_alert alter column dest_port type numeric(5);
alter table rhn_snmp_alert alter column notif_type type numeric(5);
alter table rhn_snmp_alert alter column probe_id type numeric(12);
alter table rhn_snmp_alert alter column severity type numeric(5);
alter table rhn_snmp_alert alter column command_id type numeric(12);
alter table rhn_snmp_alert alter column probe_class type numeric(5);
