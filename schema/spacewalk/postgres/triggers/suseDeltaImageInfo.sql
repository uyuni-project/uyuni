--
-- Copyright (c) 2022 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- triggers for suseImageInfo

CREATE OR REPLACE FUNCTION suse_delta_image_info_mod_trig_fun() RETURNS TRIGGER AS
$$
BEGIN
	new.modified := current_timestamp;
	return new;
END;
$$ LANGUAGE PLPGSQL;

CREATE TRIGGER suse_delta_image_info_mod_trig BEFORE INSERT OR UPDATE ON suseDeltaImageInfo
  FOR EACH ROW EXECUTE PROCEDURE suse_delta_image_info_mod_trig_fun();

CREATE OR REPLACE FUNCTION suse_delta_image_info_image_removed_trig_fun() RETURNS TRIGGER AS $$
BEGIN
  DELETE FROM suseSaltPillar WHERE id = OLD.pillar_id;
  RETURN OLD;
END $$ LANGUAGE PLPGSQL;

CREATE TRIGGER suse_delta_image_info_image_removed_trig AFTER DELETE ON suseDeltaImageInfo
  FOR EACH ROW EXECUTE PROCEDURE suse_delta_image_info_image_removed_trig_fun();
