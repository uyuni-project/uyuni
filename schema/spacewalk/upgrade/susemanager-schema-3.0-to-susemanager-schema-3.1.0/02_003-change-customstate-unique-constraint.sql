drop index suse_custom_state_name_org_uq;

CREATE UNIQUE INDEX suse_custom_state_name_org_uq
ON suseCustomState (org_id, state_name)
WHERE state_deleted='N';
