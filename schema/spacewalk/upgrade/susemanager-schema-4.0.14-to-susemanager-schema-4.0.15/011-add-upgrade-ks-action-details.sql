alter table rhnActionKickstart drop CONSTRAINT if exists rhn_actionks_up_ck;
alter table rhnActionKickstart add column if not exists
            upgrade CHAR(1) DEFAULT ('N') NOT NULL
                    CONSTRAINT rhn_actionks_up_ck
                    CHECK (upgrade in ('Y', 'N'));
