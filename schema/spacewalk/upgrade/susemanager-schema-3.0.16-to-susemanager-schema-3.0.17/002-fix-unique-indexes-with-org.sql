drop index rhn_cs_label_uq;
drop index rhn_cs_repo_uq;
CREATE UNIQUE INDEX rhn_cs_label_uq
    ON rhnContentSource(COALESCE(org_id, 0), label)
    tablespace [[64k_tbs]];
CREATE UNIQUE INDEX rhn_cs_repo_uq
    ON rhnContentSource(COALESCE(org_id, 0), type_id, source_url)
    tablespace [[64k_tbs]];

