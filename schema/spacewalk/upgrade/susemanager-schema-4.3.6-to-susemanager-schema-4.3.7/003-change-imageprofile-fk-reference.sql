ALTER TABLE rhnactivationkey ADD CONSTRAINT rhn_act_key_reg_tid_uq UNIQUE (reg_token_id);
ALTER TABLE suseimageprofile DROP CONSTRAINT suse_imgprof_tk_fk, ADD CONSTRAINT suse_imgprof_tk_fk FOREIGN KEY (token_id) REFERENCES rhnActivationKey (reg_token_id) ON DELETE SET NULL;
