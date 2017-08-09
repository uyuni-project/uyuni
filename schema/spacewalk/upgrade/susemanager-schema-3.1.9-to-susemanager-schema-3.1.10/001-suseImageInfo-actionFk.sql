ALTER TABLE suseImageInfo DROP CONSTRAINT suse_imginfo_aid_fk;
ALTER TABLE suseImageInfo DROP CONSTRAINT suse_imginfo_aid_insp_fk;

ALTER TABLE suseImageInfo ADD CONSTRAINT suse_imginfo_bldaid_fk
    FOREIGN KEY (build_action_id)
    REFERENCES rhnAction(id)
    ON DELETE SET NULL;

ALTER TABLE suseImageInfo ADD CONSTRAINT suse_imginfo_insaid_fk
    FOREIGN KEY (inspect_action_id)
    REFERENCES rhnAction(id)
    ON DELETE SET NULL;
