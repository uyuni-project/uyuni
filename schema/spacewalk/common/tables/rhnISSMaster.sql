create table rhnIssMaster (
    id NUMERIC not null constraint rhn_iss_master_id_pk primary key,
    label VARCHAR(256) not null constraint rhn_iss_master_label_uq unique,
    created TIMESTAMPTZ default (current_timestamp) not null,
    is_current_master char(1) default 'N' not null constraint rhn_issm_master_yn check (is_current_master in ('Y', 'N')),
    ca_cert VARCHAR(1024)
);

create sequence rhn_issmaster_seq;
