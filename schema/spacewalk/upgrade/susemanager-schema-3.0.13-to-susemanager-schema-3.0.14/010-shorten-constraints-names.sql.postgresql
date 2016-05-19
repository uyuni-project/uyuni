-- oracle equivalent source sha1 b14267384bc104605623a41b755e68e0103b5aa8

ALTER TABLE rhnActionApplyStatesResult DROP CONSTRAINT rhn_apply_states_result_aasid_fk;
ALTER TABLE rhnActionApplyStatesResult ADD CONSTRAINT rhn_apply_states_result_aid_fk FOREIGN KEY (action_apply_states_id) REFERENCES rhnActionApplyStates (id) ON DELETE CASCADE;

DROP INDEX rhn_apply_states_result_saas_uq;
CREATE UNIQUE INDEX rhn_apply_states_result_sa_uq
    ON rhnActionApplyStatesResult (server_id, action_apply_states_id);

DROP INDEX rhn_apply_states_result_aasid_idx;
CREATE INDEX rhn_apply_states_result_ad_idx
    ON rhnActionApplyStatesResult (action_apply_states_id);

ALTER TABLE suseServerGroupStateRevision DROP CONSTRAINT suse_server_group_rev_sid_fk;
ALTER TABLE suseServerGroupStateRevision ADD CONSTRAINT suse_server_group_gr_id_fk FOREIGN KEY (group_id) REFERENCES rhnServerGroup (id) ON DELETE CASCADE;

ALTER TABLE suseServerGroupStateRevision DROP CONSTRAINT suse_server_group_rev_id_fk;
ALTER TABLE suseServerGroupStateRevision ADD CONSTRAINT suse_server_group_sr_id_fk FOREIGN KEY (state_revision_id) REFERENCES suseStateRevision (id) ON DELETE CASCADE;

ALTER TABLE suseServerGroupStateRevision DROP CONSTRAINT suse_server_group_rev_id_sid_uq;
ALTER TABLE suseServerGroupStateRevision
    ADD CONSTRAINT suse_server_group_rev_grid_uq UNIQUE (group_id, state_revision_id);

ALTER TABLE suseServerStateRevision DROP CONSTRAINT suse_server_state_rev_id_sid_uq;
ALTER TABLE suseServerStateRevision
    ADD CONSTRAINT suse_server_state_rev_srev_uq UNIQUE (server_id, state_revision_id);

ALTER TABLE suseStateRevisionCustomState DROP CONSTRAINT suse_server_state_rev_id_fk;
ALTER TABLE suseStateRevisionCustomState ADD CONSTRAINT suse_salt_state_rev_id_fk FOREIGN KEY (state_revision_id) REFERENCES suseStateRevision (id) ON DELETE CASCADE;

