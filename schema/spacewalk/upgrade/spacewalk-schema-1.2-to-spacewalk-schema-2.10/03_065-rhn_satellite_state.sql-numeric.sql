alter table rhn_satellite_state alter column satellite_id type numeric(12);
alter table rhn_satellite_state alter column probe_count type numeric(10);
alter table rhn_satellite_state alter column recent_state_changes type numeric(10);
alter table rhn_satellite_state alter column imminent_probes type numeric(10);
