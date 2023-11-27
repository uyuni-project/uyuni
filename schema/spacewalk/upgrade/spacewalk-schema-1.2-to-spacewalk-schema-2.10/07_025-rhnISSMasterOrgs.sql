create table rhnIssMasterOrgs (
    id NUMERIC not null constraint rhn_issmasterorgs_id_pk primary key,
    master_id NUMERIC not null
        constraint rhn_issmasterorgs_cid_fk references rhnIssMaster(id) on delete cascade,
    master_org_id NUMERIC not null,
    master_org_name VARCHAR(512) not null,
    local_org_id NUMERIC
        constraint rhn_issmasterorgs_lid_fk references web_customer(id),
    created TIMESTAMPTZ default (current_timestamp) not null
);

create sequence rhn_issmasterorgs_seq;
