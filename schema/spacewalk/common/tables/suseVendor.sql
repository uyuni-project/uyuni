te table
suseVendor
(
    id      number        not null PRIMARY KEY,
    name    varchar2(256) not null,

    created     date default(sysdate) not null,
    modified    date default(sysdate) not null
);

CREATE SEQUENCE suse_vendor_id_seq START WITH 100;
