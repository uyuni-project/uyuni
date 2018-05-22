DROP INDEX suse_img_repodigest_idx;

CREATE UNIQUE INDEX suse_img_repodigest_idx
    ON suseImageRepoDigest(repo_digest, image_history_id)
    TABLESPACE [[4m_tbs]]
    NOLOGGING;
