ALTER TABLE rhnActionPlaybook ADD COLUMN IF NOT EXISTS
    flush_cache CHAR(1) DEFAULT ('N') NOT NULL
        CONSTRAINT rhn_action_playbook_flushcache_ck
            CHECK (flush_cache IN ('Y', 'N'));
