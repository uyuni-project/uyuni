create table
suseProducts
(
    id            number        not null PRIMARY KEY,
    name          varchar2(256) not null,
    version       varchar2(256),
    friendly_name varchar2(256),
    arch_type_id  NUMBER
                  CONSTRAINT suse_products_aid_fk
                  REFERENCES rhnArchType (id),
    release           varchar2(256),
    channel_family_id varchar2(256),
    product_list      CHAR(1 BYTE) DEFAULT ('N') NOT NULL ENABLE,
    vendor_id         number
                      CONSTRAINT suse_products_vid_fk
                      REFERENCES suseVendor (id),

    created     date default(sysdate) not null,
    modified    date default(sysdate) not null
);

CREATE SEQUENCE suse_products_id_seq START WITH 100;
