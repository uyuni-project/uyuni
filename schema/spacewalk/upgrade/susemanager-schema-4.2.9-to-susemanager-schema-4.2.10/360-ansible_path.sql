
CREATE TABLE IF NOT EXISTS suseAnsiblePath(

    id NUMERIC NOT NULL
        CONSTRAINT suse_ansible_path_id_pk PRIMARY KEY,

    server_id NUMERIC NOT NULL -- references minion
        CONSTRAINT suse_ansible_path_sid_fk
        REFERENCES suseMinionInfo (server_id)
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

CREATE SEQUENCE IF NOT EXISTS suse_ansible_path_seq;

CREATE UNIQUE INDEX IF NOT EXISTS suse_ansible_path_uq
    ON suseAnsiblePath(server_id, path, type);

CREATE INDEX IF NOT EXISTS suse_ansible_path_server_id_idx
    ON suseAnsiblePath(server_id);

CREATE INDEX IF NOT EXISTS suse_ansible_server_id_type_idx
    ON suseAnsiblePath(server_id, type);

CREATE OR REPLACE function suse_ansible_path_mod_trig_fun() RETURNS TRIGGER AS
$$
BEGIN
        new.modified := current_timestamp;
        RETURN new;
END;
$$ language plpgsql;

DROP TRIGGER IF EXISTS suse_ansible_path_mod_trig ON suseAnsiblePath;
CREATE TRIGGER
suse_ansible_path_mod_trig
BEFORE INSERT OR UPDATE ON suseAnsiblePath
FOR EACH ROW
EXECUTE PROCEDURE suse_ansible_path_mod_trig_fun();
