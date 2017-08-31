CREATE TABLE suseImageBuildHistory (
    id              NUMBER NOT NULL
                        CONSTRAINT suse_bldhst_id_pk PRIMARY KEY,
    image_info_id   NUMBER NOT NULL
                        CONSTRAINT suse_bldhst_imginfo_fk
                        REFERENCES suseImageInfo (id)
                        ON DELETE CASCADE,
    revision_num    NUMBER NOT NULL,
    created         TIMESTAMP WITH LOCAL TIME ZONE
                        DEFAULT (CURRENT_TIMESTAMP) NOT NULL,
    modified        TIMESTAMP WITH LOCAL TIME ZONE
                        DEFAULT (CURRENT_TIMESTAMP) NOT NULL
)
ENABLE ROW MOVEMENT
;

CREATE SEQUENCE suse_img_buildhistory_id_seq;
