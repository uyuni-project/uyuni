ALTER TABLE suseImageCustomDataValue DROP CONSTRAINT suse_icdv_prid_fk;
ALTER TABLE suseImageCustomDataValue ADD CONSTRAINT  suse_icdv_prid_fk FOREIGN KEY (image_info_id) REFERENCES suseImageInfo (id) ON DELETE CASCADE;

ALTER TABLE suseImageCustomDataValue DROP CONSTRAINT suse_icdv_kid_fk;
ALTER TABLE suseImageCustomDataValue ADD CONSTRAINT  suse_icdv_kid_fk FOREIGN KEY (key_id) REFERENCES rhnCustomDataKey (id) ON DELETE CASCADE;

ALTER TABLE suseProfileCustomDataValue DROP CONSTRAINT suse_pcdv_prid_fk;
ALTER TABLE suseProfileCustomDataValue ADD CONSTRAINT  suse_pcdv_prid_fk FOREIGN KEY (profile_id) REFERENCES suseImageProfile (profile_id) ON DELETE CASCADE;

ALTER TABLE suseProfileCustomDataValue DROP CONSTRAINT suse_pcdv_kid_fk;
ALTER TABLE suseProfileCustomDataValue ADD CONSTRAINT  suse_pcdv_kid_fk FOREIGN KEY (key_id) REFERENCES rhnCustomDataKey (id) ON DELETE CASCADE;

ALTER TABLE suseImageInfo DROP CONSTRAINT rhn_imginfo_said_fk;
ALTER TABLE suseImageInfo ADD CONSTRAINT suse_imginfo_said_fk FOREIGN KEY (image_arch_id) REFERENCES rhnServerArch (id);
