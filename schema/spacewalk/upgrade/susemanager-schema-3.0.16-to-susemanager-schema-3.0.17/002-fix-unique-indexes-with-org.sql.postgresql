-- oracle equivalent source sha1 364a751fd67bac72868f477c4fe532ec714ba48d

drop index if exists rhn_cs_label_uq;
CREATE UNIQUE INDEX rhn_cs_label_uq
    ON rhnContentSource(COALESCE(org_id, 0), label)
    ;
drop index if exists rhn_cs_repo_uq;
CREATE UNIQUE INDEX rhn_cs_repo_uq
    ON rhnContentSource(COALESCE(org_id, 0), type_id, source_url)
    ;
