CREATE TABLE IF NOT EXISTS suseImageFile
(
    id             NUMERIC NOT NULL
                     CONSTRAINT suse_imgfile_fileid_pk PRIMARY KEY,
    image_info_id  NUMERIC NOT NULL,
    file           TEXT NOT NULL,
    type           VARCHAR(16) NOT NULL,
    external       CHAR(1) DEFAULT ('N') NOT NULL,
    created        TIMESTAMPTZ
                     DEFAULT (current_timestamp) NOT NULL,
    modified       TIMESTAMPTZ
                     DEFAULT (current_timestamp) NOT NULL,

    CONSTRAINT suse_imgfile_imginfo_fk FOREIGN KEY (image_info_id)
        REFERENCES suseImageInfo (id) ON DELETE CASCADE
);

CREATE SEQUENCE IF NOT EXISTS suse_image_file_id_seq;
