CREATE OR REPLACE FUNCTION suse_image_info_image_removed_trig_fun() RETURNS TRIGGER AS $$
BEGIN
  DELETE FROM suseSaltPillar WHERE id = OLD.pillar_id;
  DELETE FROM suseSaltPillar WHERE category = concat('SyncedImage', OLD.id);
  RETURN OLD;
END $$ LANGUAGE PLPGSQL;
