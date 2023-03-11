
alter table rhn_url_probe_step alter column connect_warn type numeric(10,3);
alter table rhn_url_probe_step alter column connect_crit type numeric(10,3);
alter table rhn_url_probe_step alter column latency_warn type numeric(10,3);
alter table rhn_url_probe_step alter column latency_crit type numeric(10,3);
alter table rhn_url_probe_step alter column dns_warn     type numeric(10,3);
alter table rhn_url_probe_step alter column dns_crit     type numeric(10,3);
alter table rhn_url_probe_step alter column total_warn   type numeric(10,3);
alter table rhn_url_probe_step alter column total_crit   type numeric(10,3);
