-- Archive actions which have no servers assigned
UPDATE rhnAction SET archived = 1 WHERE id IN (
    SELECT id FROM rhnUserActionOverview
    WHERE user_id IS NULL
);
