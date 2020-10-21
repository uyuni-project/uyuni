INSERT INTO rhnTaskoBunch (id, name, description, org_bunch)
SELECT sequence_nextval('rhn_tasko_bunch_id_seq'), 'ssh-minion-action-executor-bunch', 'Execute actions on SSH Minions', null
WHERE NOT EXISTS (
    SELECT 1 FROM rhnTaskoBunch WHERE name='ssh-minion-action-executor-bunch'
);

INSERT INTO rhnTaskoTask (id, name, class)
SELECT sequence_nextval('rhn_tasko_task_id_seq'), 'ssh-minion-action-executor', 'com.redhat.rhn.taskomatic.task.SSHMinionActionExecutor'
WHERE NOT EXISTS (
    SELECT 1 FROM rhnTaskoTask WHERE name='ssh-minion-action-executor'
);

INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
SELECT sequence_nextval('rhn_tasko_template_id_seq'),
        (SELECT id FROM rhnTaskoBunch WHERE name='ssh-minion-action-executor-bunch'),
        (SELECT id FROM rhnTaskoTask WHERE name='ssh-minion-action-executor'),
        0,
        null
WHERE NOT EXISTS (
    SELECT 1 FROM rhnTaskoTemplate WHERE bunch_id=(SELECT id FROM rhnTaskoBunch WHERE name='ssh-minion-action-executor-bunch')
);
