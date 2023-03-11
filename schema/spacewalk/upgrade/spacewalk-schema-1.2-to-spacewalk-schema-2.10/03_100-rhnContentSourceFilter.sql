
create table
rhnContentSourceFilter
(
        id		NUMERIC NOT NULL
			constraint rhn_csf_id_pk primary key,
        source_id		NUMERIC
			constraint rhn_csf_source_fk
                                references rhnContentSource (id),
        sort_order	NUMERIC NOT NULL,
        flag            VARCHAR(1) NOT NULL
                        check (flag in ('+','-')),
        filter          VARCHAR(4000) NOT NULL,
        created         TIMESTAMPTZ default(CURRENT_TIMESTAMP) NOT NULL,
        modified        TIMESTAMPTZ default(CURRENT_TIMESTAMP) NOT NULL
)

  ;


create sequence rhn_csf_id_seq start with 500;

CREATE UNIQUE INDEX rhn_csf_sid_so_uq
    ON rhnContentSourceFilter (source_id, sort_order)
    ;

