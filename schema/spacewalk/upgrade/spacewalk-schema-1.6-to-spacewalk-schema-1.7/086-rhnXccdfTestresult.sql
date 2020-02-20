
CREATE TABLE rhnXccdfTestresult
(
    id             NUMERIC NOT NULL
                       CONSTRAINT rhn_xccdf_tresult_id_pk PRIMARY KEY
                       ,
    server_id      NUMERIC NOT NULL
                       CONSTRAINT rhn_xccdf_tresult_srvr_fk
                           REFERENCES rhnServer (id)
                           ON DELETE CASCADE,
    action_scap_id NUMERIC NOT NULL
                       CONSTRAINT rhn_xccdf_tresult_act_fk
                           REFERENCES rhnActionScap (id)
                           ON DELETE CASCADE,
    benchmark_id   NUMERIC NOT NULL
                       CONSTRAINT rhn_xccdf_tresult_bench_fk
                           REFERENCES rhnXccdfBenchmark (id),
    profile_id     NUMERIC NOT NULL
                       CONSTRAINT rhn_xccdf_tresult_profile_fk
                           REFERENCES rhnXccdfProfile (id),
    identifier     VARCHAR(120) NOT NULL,
    start_time     TIMESTAMPTZ,
    end_time       TIMESTAMPTZ NOT NULL,
    errors         BYTEA
)


;

CREATE UNIQUE INDEX rhn_xccdf_tresult_sa_uq
    ON rhnXccdfTestresult (server_id, action_scap_id)
    
    ;

CREATE SEQUENCE rhn_xccdf_tresult_id_seq;
