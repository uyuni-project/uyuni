--
-- Copyright (c) 2012 Novell, Inc.
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

-- Insert task and map to the Java class
INSERT INTO rhnTaskoTask (id, name, class)
    VALUES (sequence_nextval('rhn_tasko_task_id_seq'), 'ssh-server-push',
        'com.redhat.rhn.taskomatic.task.SSHServerPush');

-- Insert bunch
INSERT INTO rhnTaskoBunch (id, name, description, org_bunch)
    VALUES (sequence_nextval('rhn_tasko_bunch_id_seq'), 'ssh-server-push-bunch',
        'Push scheduled actions to clients via ssh', null);

-- Insert template
INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
    VALUES (sequence_nextval('rhn_tasko_template_id_seq'),
        (SELECT id FROM rhnTaskoBunch WHERE name='ssh-server-push-bunch'),
        (SELECT id FROM rhnTaskoTask WHERE name='ssh-server-push'),
        0, null);

-- Insert schedule for task (run once every minute)
INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES (sequence_nextval('rhn_tasko_schedule_id_seq'), 'ssh-server-push-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='ssh-server-push-bunch'),
        current_timestamp, '0 * * * * ?');
