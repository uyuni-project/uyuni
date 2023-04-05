
CREATE TABLE rhnXccdfBenchmark
(
    id            NUMERIC NOT NULL
                      CONSTRAINT rhn_xccdf_benchmark_id_pk PRIMARY KEY
                      ,
    identifier    VARCHAR(120) NOT NULL,
    version       VARCHAR(80) NOT NULL
)

;

CREATE UNIQUE INDEX rhn_xccdf_benchmark_iv_uq
    ON rhnXccdfBenchmark (identifier, version)
    
    ;

CREATE SEQUENCE rhn_xccdf_benchmark_id_seq;
