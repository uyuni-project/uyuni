alter table rhn_probe alter column customer_id type numeric(12);
alter table rhn_probe alter column command_id type numeric(16);
alter table rhn_probe alter column contact_group_id type numeric(12);
alter table rhn_probe alter column notification_interval_minutes type numeric(16);
alter table rhn_probe alter column check_interval_minutes type numeric(16);
alter table rhn_probe alter column retry_interval_minutes type numeric(16);
alter table rhn_probe alter column max_attempts type numeric(16);
