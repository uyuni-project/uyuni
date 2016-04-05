-- functional index for suseCustomState

CREATE UNIQUE INDEX suse_custom_state_name_org_uq
ON suseCustomState(
 CASE WHEN state_deleted = 'N' then org_id END,
 CASE WHEN state_deleted = 'N' then state_name END
);
