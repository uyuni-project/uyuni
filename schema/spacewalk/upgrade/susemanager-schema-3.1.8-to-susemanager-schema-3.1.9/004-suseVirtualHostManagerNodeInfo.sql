CREATE TABLE suseVirtualHostManagerNodeInfo
(
    id            NUMBER NOT NULL
                    CONSTRAINT suse_vhmnode_id_pk PRIMARY KEY,
    identifier    VARCHAR2(1024) NOT NULL,
    name          VARCHAR2(128),
    node_arch_id  NUMBER
                    CONSTRAINT rhn_server_said_fk
                    REFERENCES rhnServerArch (id),
    cpu_sockets   NUMBER,
    cpu_cores     NUMBER,
    ram           NUMBER,
    os            VARCHAR2(64) NOT NULL,
    os_version    VARCHAR2(64) NOT NULL,
    created       timestamp with local time zone
                      DEFAULT (current_timestamp) NOT NULL,
    modified      timestamp with local time zone
                      DEFAULT (current_timestamp) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE SEQUENCE suse_vhm_nodeinfo_id_seq;
