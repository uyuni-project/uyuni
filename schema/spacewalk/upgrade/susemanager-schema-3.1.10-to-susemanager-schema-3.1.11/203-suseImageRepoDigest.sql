CREATE TABLE suseImageRepoDigest (
    id                  NUMERIC NOT NULL
                            CONSTRAINT suse_rdigest_id_pk PRIMARY KEY,
    image_history_id    NUMERIC NOT NULL
                            CONSTRAINT suse_rdigest_bldhst_fk
                            REFERENCES suseImageBuildHistory (id)
                            ON DELETE CASCADE,
    repo_digest         VARCHAR(255) NOT NULL,
    created             TIMESTAMPTZ
                            DEFAULT (CURRENT_TIMESTAMP) NOT NULL,
    modified            TIMESTAMPTZ
                            DEFAULT (CURRENT_TIMESTAMP) NOT NULL
)

;

CREATE UNIQUE INDEX suse_img_repodigest_idx
    ON suseImageRepoDigest(repo_digest)
    
    ;

CREATE SEQUENCE suse_img_repodigest_id_seq;
