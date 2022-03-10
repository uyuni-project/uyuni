ALTER TABLE suseImageInfo ADD COLUMN IF NOT EXISTS obsolete       CHAR(1) DEFAULT ('N') NOT NULL;

ALTER TABLE suseImageInfo ADD COLUMN IF NOT EXISTS built          CHAR(1) DEFAULT ('N') NOT NULL;

ALTER TABLE suseImageInfo ADD COLUMN IF NOT EXISTS pillar_id      NUMERIC
                        CONSTRAINT suse_imginfo_pillar_fk
                        REFERENCES suseSaltPillar (id)
                        ON DELETE SET NULL;

ALTER TABLE suseImageInfo ADD COLUMN IF NOT EXISTS log            TEXT;

UPDATE suseImageInfo SET built='Y' WHERE id IN (SELECT DISTINCT image_info_id FROM suseImageInfoPackage);

CREATE OR REPLACE FUNCTION suse_image_info_image_removed_trig_fun() RETURNS TRIGGER AS $$
BEGIN
  DELETE FROM suseSaltPillar WHERE id = OLD.pillar_id;
  RETURN OLD;
END $$ LANGUAGE PLPGSQL;

DROP TRIGGER IF EXISTS suse_image_info_image_removed_trig ON suseImageInfo;
CREATE TRIGGER suse_image_info_image_removed_trig AFTER DELETE ON suseImageInfo
  FOR EACH ROW EXECUTE PROCEDURE suse_image_info_image_removed_trig_fun();

