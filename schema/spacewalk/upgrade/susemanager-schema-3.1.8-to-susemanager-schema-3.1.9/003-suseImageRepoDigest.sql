CREATE TABLE suseImageRepoDigest (
    id                  NUMBER NOT NULL
                            CONSTRAINT suse_rdigest_id_pk PRIMARY KEY,
    image_history_id    NUMBER NOT NULL
                            CONSTRAINT suse_rdigest_bldhst_fk
                            REFERENCES suseImageBuildHistory (id)
                            ON DELETE CASCADE,
    repo_digest         VARCHAR2(255) NOT NULL,
    created             TIMESTAMP WITH LOCAL TIME ZONE
                            DEFAULT (CURRENT_TIMESTAMP) NOT NULL,
    modified            TIMESTAMP WITH LOCAL TIME ZONE
                            DEFAULT (CURRENT_TIMESTAMP) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE UNIQUE INDEX suse_img_repodigest_idx
    ON suseImageRepoDigest(repo_digest)
    TABLESPACE [[64k_tbs]]
    NOLOGGING;

CREATE SEQUENCE suse_img_repodigest_id_seq;
