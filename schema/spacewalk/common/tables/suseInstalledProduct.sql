create table
suseInstalledProduct
(
    id            number        not null PRIMARY KEY,
    name          varchar2(256) not null,
    version       varchar2(256),
    arch_type_id  NUMBER
                  CONSTRAINT suse_installed_product_aid_fk
                  REFERENCES rhnArchType (id),
    release       varchar2(256),
    is_baseproduct CHAR(1 BYTE) DEFAULT ('N') NOT NULL ENABLE,

    created     date default(sysdate) not null,
    modified    date default(sysdate) not null
);

CREATE SEQUENCE suse_inst_pr_id_seq START WITH 100;
