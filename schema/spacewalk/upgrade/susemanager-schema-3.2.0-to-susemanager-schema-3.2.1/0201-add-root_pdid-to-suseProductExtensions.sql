alter table suseProductExtension add root_pdid number not null CONSTRAINT suse_prdext_rootid_fk REFERENCES suseProducts (id) ON DELETE CASCADE;
alter table suseProductExtension add recommended CHAR(1) DEFAULT ('N') NOT NULL CONSTRAINT suse_prdext_rec_ck CHECK (recommended in ('Y', 'N'));

CREATE UNIQUE INDEX prdext_ber_id_uq
ON suseProductExtension (base_pdid, ext_pdid, root_pdid)
TABLESPACE [[64k_tbs]];
