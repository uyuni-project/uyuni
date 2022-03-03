ALTER TABLE public.rhnactionimagebuild DROP CONSTRAINT rhn_act_image_build_ip_fk;
ALTER TABLE public.rhnactionimagebuild ADD CONSTRAINT rhn_act_image_build_ip_fk FOREIGN KEY (image_profile_id) REFERENCES suseimageprofile(profile_id) ON DELETE SET NULL;
ALTER TABLE public.rhnactionimageinspect DROP CONSTRAINT rhn_act_image_inspect_is_fk;
ALTER TABLE public.rhnactionimageinspect ADD CONSTRAINT rhn_act_image_inspect_is_fk FOREIGN KEY (image_store_id) REFERENCES suseImageStore(id) ON DELETE SET NULL;
