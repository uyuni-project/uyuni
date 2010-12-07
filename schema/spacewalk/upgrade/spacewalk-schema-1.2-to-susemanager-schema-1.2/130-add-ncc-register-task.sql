--
-- Copyright (c) 2010 Novell
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--

INSERT INTO rhnTaskoTask (id, name, class)
     VALUES (sequence_nextval('rhn_tasko_task_id_seq'), 'sm-register', 'com.redhat.rhn.taskomatic.task.NccRegisterTask');

INSERT INTO rhnTaskoBunch (id, name, description, org_bunch)
     VALUES (sequence_nextval('rhn_tasko_bunch_id_seq'), 'sm-register-bunch', 'Runs sm-register', 'Y');

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
     VALUES(sequence_nextval('rhn_tasko_schedule_id_seq'), 'sm-register-default',
            (SELECT id FROM rhnTaskoBunch WHERE name='sm-register-bunch'),
            current_timestamp, '0 0/15 * * * ?');

INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
     VALUES (sequence_nextval('rhn_tasko_template_id_seq'),
             (SELECT id FROM rhnTaskoBunch WHERE name='sm-register-bunch'),
             (SELECT id FROM rhnTaskoTask WHERE name='sm-register'),
             0,
             null);

commit;

