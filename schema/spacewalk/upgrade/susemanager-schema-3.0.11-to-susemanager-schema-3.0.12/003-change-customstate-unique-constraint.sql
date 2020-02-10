-- oracle equivalent source sha1 c67bda22571c6c50798c0f60f808254d9de208a4
drop index suse_custom_state_name_org_uq;

CREATE UNIQUE INDEX suse_custom_state_name_org_uq
ON suseCustomState (org_id, state_name)
WHERE state_deleted='N';
