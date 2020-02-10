alter table rhn_contact_methods alter column contact_id type numeric(12);
alter table rhn_contact_methods alter column schedule_id type numeric(12);
alter table rhn_contact_methods alter column method_type_id type numeric(12);
alter table rhn_contact_methods alter column pager_type_id type numeric(12);
alter table rhn_contact_methods alter column pager_max_message_length type numeric(6);
alter table rhn_contact_methods alter column snmp_port type numeric(5);
alter table rhn_contact_methods alter column notification_format_id type numeric(12);
alter table rhn_contact_methods alter column sender_sat_cluster_id type numeric(12);
