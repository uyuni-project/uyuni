CREATE TABLE suseDeltaImageInfo
(
    source_image_id NUMERIC NOT NULL
                     CONSTRAINT suse_deltaimg_source_fk
                     REFERENCES suseImageInfo (id) ON DELETE CASCADE,

    target_image_id NUMERIC NOT NULL
                     CONSTRAINT suse_deltaimg_target_fk
                     REFERENCES suseImageInfo (id) ON DELETE CASCADE,

    pillar_id       NUMERIC
                     CONSTRAINT suse_deltaimg_pillar_fk
                       REFERENCES suseSaltPillar (id)
                       ON DELETE SET NULL,

    file           TEXT NOT NULL,

    created        TIMESTAMPTZ
                     DEFAULT (current_timestamp) NOT NULL,
    modified       TIMESTAMPTZ
                     DEFAULT (current_timestamp) NOT NULL,

    CONSTRAINT suse_delta_image_info_pk PRIMARY KEY (source_image_id, target_image_id)
);

