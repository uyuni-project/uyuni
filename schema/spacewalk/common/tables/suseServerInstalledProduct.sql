create table
suseServerInstalledProduct
(
    rhn_server_id     number
                      CONSTRAINT suseserver_ip_rhns_id_fk
                      REFERENCES rhnserver (id)
                      ON DELETE CASCADE
                      not null,
    suse_installed_product_id   number
                                CONSTRAINT ssip_sip_id_fk
                                REFERENCES suseInstalledProduct (id)
                                not null,

    created     date default(sysdate) not null,
    modified    date default(sysdate) not null
);

