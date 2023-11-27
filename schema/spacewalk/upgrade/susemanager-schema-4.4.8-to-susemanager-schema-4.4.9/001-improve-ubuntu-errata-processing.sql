INSERT INTO rhnTaskoTask (id, name, class)
     SELECT sequence_nextval('rhn_tasko_task_id_seq')
                , 'ubuntu-errata'
                , 'com.redhat.rhn.taskomatic.task.UbuntuErrataTask'
      WHERE NOT EXISTS (SELECT id FROM rhnTaskoTask WHERE name = 'ubuntu-errata');

INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
    SELECT sequence_nextval('rhn_tasko_template_id_seq')
                , tb.id
                , tt.id
                , 1
                , 'FINISHED'
      FROM rhnTaskoBunch tb, rhnTaskoTask tt
    WHERE tb.name = 'repo-sync-bunch'
                AND tt.name = 'ubuntu-errata'
                AND NOT EXISTS (SELECT id FROM rhnTaskoTemplate WHERE bunch_id = tb.id AND task_id = tt.id);
