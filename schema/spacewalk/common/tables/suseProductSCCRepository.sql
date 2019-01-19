CREATE TABLE
suseProductSCCRepository
(
    id                     NUMBER NOT NULL primary key,
    product_id             NUMBER NOT NULL
                                  CONSTRAINT suse_prdrepo_pid_fk
                                  REFERENCES suseProducts (id)
                                  ON DELETE CASCADE,
    root_product_id        NUMBER NOT NULL
                                  CONSTRAINT suse_prdrepo_rpid_fk
                                  REFERENCES suseProducts (id)
                                  ON DELETE CASCADE,
    repo_id                NUMBER NOT NULL
                                  CONSTRAINT suse_prdrepo_rid_fk
                                  REFERENCES suseSCCRepository (id)
                                  ON DELETE CASCADE,
    channel_label          VARCHAR2(128) not null,
    parent_channel_label   VARCHAR2(128),
    channel_name           VARCHAR2(256) not null,
    mandatory              CHAR(1) DEFAULT ('N') NOT NULL
                                   CONSTRAINT suse_prdrepo_mand_ck
                                   CHECK (mandatory in ('Y', 'N')),
    installer_updates      CHAR(1) DEFAULT ('N') NOT NULL
                                   CONSTRAINT suse_prdrepo_instup_ck
                                   CHECK (installer_updates in ('Y', 'N')),
    update_tag             VARCHAR2(128),
    created                timestamp with local time zone
                           DEFAULT (current_timestamp) NOT NULL,
    modified               timestamp with local time zone
                           DEFAULT (current_timestamp) NOT NULL
);

CREATE SEQUENCE suse_prdrepo_id_seq START WITH 1;

CREATE UNIQUE INDEX suse_prdrepo_pid_rpid_rid_uq
ON suseProductSCCRepository (product_id, root_product_id, repo_id)
TABLESPACE [[64k_tbs]];

CREATE INDEX suse_prdrepo_chl_idx
ON suseProductSCCRepository (channel_label)
TABLESPACE [[64k_tbs]];
