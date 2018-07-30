-- oracle equivalent source sha1 7f8d457f039ebebd1a3a9e7560ae51bb47e89609

DROP INDEX IF EXISTS suse_imgstore_label_uq;
DROP INDEX IF EXISTS suse_imgstore_oid_label_uq;

CREATE UNIQUE INDEX suse_imgstore_oid_label_uq
    ON suseImageStore (org_id, label);
