-- oracle equivalent source none
CREATE TABLE IF NOT EXISTS rhnActionVirtCreate
(
    action_id            NUMERIC NOT NULL
                             CONSTRAINT rhn_action_virt_create_aid_fk
                                 REFERENCES rhnAction (id)
                                 ON DELETE CASCADE
                             CONSTRAINT rhn_action_virt_create_aid_pk
                                 PRIMARY KEY,
    uuid                 VARCHAR(128),
    vm_type              VARCHAR(10),
    vm_name              VARCHAR(256),
    os_type              VARCHAR(20),
    memory               NUMERIC,
    vcpus                NUMERIC,
    arch                 VARCHAR(20),
    graphics_type        VARCHAR(20),
    remove_disks         CHAR(1),
    remove_interfaces    CHAR(1)
);

CREATE UNIQUE INDEX IF NOT EXISTS rhn_action_virt_create_aid_uq
    ON rhnActionVirtCreate (action_id);


CREATE TABLE IF NOT EXISTS rhnActionVirtCreateDiskDetails
(
    id                   NUMERIC NOT NULL
                             CONSTRAINT rhn_action_virt_create_disk_details_id_pk
                                 PRIMARY KEY,
    type                 VARCHAR(15),
    device               VARCHAR(10),
    template             VARCHAR(256),
    size                 NUMERIC,
    bus                  VARCHAR(10),
    pool                 VARCHAR(256),
    source_file          VARCHAR(1024),
    idx                  NUMERIC,
    action_id            NUMERIC NOT NULL
                             CONSTRAINT rhn_action_virt_create_disk_details_aid_fk
                                 REFERENCES rhnActionVirtCreate (action_id)
                                 ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS rhn_action_virt_create_disk_details_id_idx
    ON rhnActionVirtCreateDiskDetails (id);

CREATE SEQUENCE IF NOT EXISTS rhn_action_virt_create_disk_details_id_seq;


CREATE TABLE IF NOT EXISTS rhnActionVirtCreateInterfaceDetails
(
    id                   NUMERIC NOT NULL
                             CONSTRAINT rhn_action_virt_create_iface_details_id_pk
                                 PRIMARY KEY,
    type                 VARCHAR(20),
    source               VARCHAR(256),
    mac                  VARCHAR(20),
    idx                  NUMERIC,
    action_id            NUMERIC NOT NULL
                             CONSTRAINT rhn_action_virt_create_iface_details_aid_fk
                                 REFERENCES rhnActionVirtCreate (action_id)
                                 ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS rhn_action_virt_create_iface_details_id_idx
    ON rhnActionVirtCreateInterfaceDetails (id);

CREATE SEQUENCE IF NOT EXISTS rhn_action_virt_create_iface_details_id_seq;
