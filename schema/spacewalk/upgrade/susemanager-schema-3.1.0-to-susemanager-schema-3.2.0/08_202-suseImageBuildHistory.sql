CREATE TABLE suseImageBuildHistory (
    id              NUMERIC NOT NULL
                        CONSTRAINT suse_bldhst_id_pk PRIMARY KEY,
    image_info_id   NUMERIC NOT NULL
                        CONSTRAINT suse_bldhst_imginfo_fk
                        REFERENCES suseImageInfo (id)
                        ON DELETE CASCADE,
    revision_num    NUMERIC NOT NULL,
    created         TIMESTAMPTZ
                        DEFAULT (CURRENT_TIMESTAMP) NOT NULL,
    modified        TIMESTAMPTZ
                        DEFAULT (CURRENT_TIMESTAMP) NOT NULL
)

;

CREATE SEQUENCE suse_img_buildhistory_id_seq;
