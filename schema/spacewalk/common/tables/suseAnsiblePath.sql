
CREATE TABLE suseAnsiblePath(

    id NUMERIC NOT NULL
        CONSTRAINT suse_ansible_path_id_pk PRIMARY KEY,

    server_id NUMERIC NOT NULL -- references minion
        CONSTRAINT suse_ansible_path_sid_fk
        REFERENCES suseMinionInfo (server_id)
        ON DELETE CASCADE,

    path VARCHAR(1024) NOT NULL,

    type VARCHAR(16) NOT NULL
        CONSTRAINT suse_ansible_path_type_ck
        CHECK (type in ('inventory', 'playbook'))

);

CREATE SEQUENCE suse_ansible_path_seq;

CREATE UNIQUE INDEX suse_ansible_path_uq
    ON suseAnsiblePath(server_id, path, type);

CREATE INDEX suse_ansible_path_server_id_idx
    ON suseAnsiblePath(server_id);

CREATE INDEX suse_ansible_server_id_type_idx
    ON suseAnsiblePath(server_id, type);

