ALTER TABLE rhnErrata DROP CONSTRAINT IF EXISTS rhn_errata_adv_status_ck;
ALTER TABLE rhnErrata ADD COLUMN IF NOT EXISTS
    advisory_status   VARCHAR(32) NOT NULL DEFAULT('final')
                          CONSTRAINT rhn_errata_adv_status_ck
                             CHECK (advisory_status in ('final', 'stable', 'testing',
                                                        'retracted'));
