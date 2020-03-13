
DROP INDEX IF EXISTS suse_imgprof_label_uq;
DROP INDEX IF EXISTS suse_imgprof_oid_label_uq;

CREATE UNIQUE INDEX suse_imgprof_oid_label_uq
    ON suseImageProfile (org_id, label);
