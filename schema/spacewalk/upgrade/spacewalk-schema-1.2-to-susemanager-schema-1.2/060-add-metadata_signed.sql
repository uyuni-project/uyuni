
alter table rhnContentSource
add metadata_signed char(1) default('Y')
                constraint rhn_cs_ms_nn not null
                constraint rhn_cs_ms_ck
                check (metadata_signed in ('Y','N'));

