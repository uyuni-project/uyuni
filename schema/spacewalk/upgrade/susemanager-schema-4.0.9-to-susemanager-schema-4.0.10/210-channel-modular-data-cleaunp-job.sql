INSERT INTO rhnTaskoTask (id, name, class)
SELECT sequence_nextval('rhn_tasko_task_id_seq'), 'channel-modular-data-cleanup', 'com.redhat.rhn.taskomatic.task.ModularDataCleanup'
WHERE NOT EXISTS (SELECT 1 FROM rhnTaskoTask WHERE name='channel-modular-data-cleanup');

INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
SELECT sequence_nextval('rhn_tasko_template_id_seq'),
    (SELECT id FROM rhnTaskoBunch WHERE name='cleanup-data-bunch'),
    (SELECT id FROM rhnTaskoTask WHERE name='channel-modular-data-cleanup'),
    1,
    null
WHERE NOT EXISTS (
    SELECT 1 FROM rhnTaskoTemplate
    WHERE bunch_id = (SELECT id FROM rhnTaskoBunch WHERE name = 'cleanup-data-bunch')
      AND task_id = (SELECT id FROM rhnTaskoTask WHERE name = 'channel-modular-data-cleanup')
      AND ordering = 1
);
