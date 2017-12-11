ALTER TABLE rhnActionApplyStates ADD test CHAR(1) DEFAULT ('N') NOT NULL CONSTRAINT rhn_act_apply_states_test_ck CHECK (test in ('Y', 'N'));

