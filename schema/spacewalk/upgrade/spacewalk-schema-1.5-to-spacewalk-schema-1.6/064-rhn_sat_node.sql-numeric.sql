alter table rhn_sat_node alter column recid type numeric(12);
alter table rhn_sat_node alter column max_concurrent_checks type numeric(4);
alter table rhn_sat_node alter column sat_cluster_id type numeric(12);
alter table rhn_sat_node alter column sched_log_level type numeric(4);
alter table rhn_sat_node alter column sput_log_level type numeric(4);
alter table rhn_sat_node alter column dq_log_level type numeric(4);
