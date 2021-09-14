ALTER TABLE rhnErrata DROP CONSTRAINT IF EXISTS rhn_errata_adv_status_ck;
ALTER TABLE rhnErrata ADD
    CONSTRAINT rhn_errata_adv_status_ck
    CHECK (advisory_status in ('final', 'stable', 'testing', 'pending', 'retracted'));

