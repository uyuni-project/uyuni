-- oracle equivalent source sha1 df2f6fdeca774648428333d2dbf3dd82e2c7d220
-- functional index for suseCustomState

CREATE UNIQUE INDEX suse_custom_state_name_org_uq
ON suseCustomState (org_id, state_name)
WHERE state_deleted='N';
