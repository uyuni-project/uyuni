-- Add a new taskomatic task for running subscription matcher.
INSERT INTO rhnTaskoTask (id, name, class)
         VALUES (sequence_nextval('rhn_tasko_task_id_seq'), 'matcher', 'com.redhat.rhn.taskomatic.task.matcher.MatcherJob');

-- Add this task to the same bunch with the gatherer task. The matcher task will be run AFTER the gatherer one.
INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
             VALUES (sequence_nextval('rhn_tasko_template_id_seq'),
                        (SELECT id FROM rhnTaskoBunch WHERE name='gatherer-matcher-bunch'),
                        (SELECT id FROM rhnTaskoTask WHERE name='matcher'),
                        1,
                        'FINISHED');

