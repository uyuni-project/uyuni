-- oracle equivalent source sha1 0ebd9f3affc795e8716a843774f712ac0f3d3756

DROP INDEX IF EXISTS suse_imgprof_label_uq;
DROP INDEX IF EXISTS suse_imgprof_oid_label_uq;

CREATE UNIQUE INDEX suse_imgprof_oid_label_uq
    ON suseImageProfile (org_id, label);
