ALTER TABLE rhnActionPlaybook ADD COLUMN IF NOT EXISTS
    test_mode CHAR(1) DEFAULT ('N') NOT NULL
        CONSTRAINT rhn_action_playbook_testmode_ck
            CHECK (test_mode IN ('Y', 'N'));
