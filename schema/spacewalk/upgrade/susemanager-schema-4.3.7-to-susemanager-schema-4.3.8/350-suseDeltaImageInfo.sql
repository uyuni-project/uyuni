CREATE TABLE IF NOT EXISTS suseDeltaImageInfo
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

    CONSTRAINT suse_deltaimg_pk PRIMARY KEY (source_image_id, target_image_id)
);

CREATE OR REPLACE FUNCTION suse_delta_image_info_mod_trig_fun() RETURNS TRIGGER AS
$$
BEGIN
        new.modified := current_timestamp;
        return new;
END;
$$ LANGUAGE PLPGSQL;

DROP TRIGGER IF EXISTS suse_delta_image_info_mod_trig ON suseDeltaImageInfo;
CREATE TRIGGER suse_delta_image_info_mod_trig BEFORE INSERT OR UPDATE ON suseDeltaImageInfo
  FOR EACH ROW EXECUTE PROCEDURE suse_delta_image_info_mod_trig_fun();

CREATE OR REPLACE FUNCTION suse_delta_image_info_image_removed_trig_fun() RETURNS TRIGGER AS $$
BEGIN
  DELETE FROM suseSaltPillar WHERE id = OLD.pillar_id;
  RETURN OLD;
END $$ LANGUAGE PLPGSQL;

DROP TRIGGER IF EXISTS suse_delta_image_info_image_removed_trig ON suseDeltaImageInfo;
CREATE TRIGGER suse_delta_image_info_image_removed_trig AFTER DELETE ON suseDeltaImageInfo
  FOR EACH ROW EXECUTE PROCEDURE suse_delta_image_info_image_removed_trig_fun();
