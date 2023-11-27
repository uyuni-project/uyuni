
DROP INDEX IF EXISTS suse_imgstore_label_uq;
DROP INDEX IF EXISTS suse_imgstore_oid_label_uq;

CREATE UNIQUE INDEX suse_imgstore_oid_label_uq
    ON suseImageStore (org_id, label);
