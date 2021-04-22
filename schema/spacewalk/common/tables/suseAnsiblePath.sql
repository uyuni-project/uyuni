
CREATE TABLE suseAnsiblePath(

    id NUMERIC NOT NULL
        CONSTRAINT suse_ansible_path_id_pk PRIMARY KEY,

    server_id NUMERIC NOT NULL
        CONSTRAINT suse_ansible_path_sid_fk
        REFERENCES rhnServer(id)
        ON DELETE CASCADE,

    path VARCHAR(1024) NOT NULL,

    type VARCHAR(16) NOT NULL
        CONSTRAINT suse_ansible_path_type_ck
        CHECK (type in ('inventory', 'playbook')),

    created     TIMESTAMPTZ
        DEFAULT (current_timestamp) NOT NULL,

    modified    TIMESTAMPTZ
        DEFAULT (current_timestamp) NOT NULL
);

CREATE SEQUENCE suse_ansible_path_seq;

CREATE UNIQUE INDEX suse_ansible_path_type_uq
    ON suseAnsiblePath(server_id, path, type);

CREATE UNIQUE INDEX suse_ansible_type_path_uq
    ON suseAnsiblePath(server_id, type, path);

