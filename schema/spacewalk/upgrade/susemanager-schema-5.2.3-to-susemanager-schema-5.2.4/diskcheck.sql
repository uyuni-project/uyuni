--
-- Copyright (c) 2026 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

INSERT INTO rhnTaskoBunch (id, name, description, org_bunch)
  SELECT sequence_nextval('rhn_tasko_bunch_id_seq'), 'diskcheck-task-bunch', 'Schedules a disk check on server and DB', null FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM rhnTaskoBunch WHERE name = 'diskcheck-task-bunch');
  
INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
  SELECT sequence_nextval('rhn_tasko_template_id_seq'),
                         (SELECT id FROM rhnTaskoBunch WHERE name='diskcheck-task-bunch'),
                         (SELECT id FROM rhnTaskoTask WHERE name='diskcheck-task'),
                         0, null FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM rhnTaskoTemplate
                    WHERE  bunch_id = (SELECT id FROM rhnTaskoBunch WHERE name='diskcheck-task-bunch')
                    AND    task_id = (SELECT id FROM rhnTaskoTask WHERE name='diskcheck-task'));
                    
INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
  SELECT sequence_nextval('rhn_tasko_schedule_id_seq'), 'diskcheck-task-queue-default',
         (SELECT id FROM rhnTaskoBunch WHERE name='diskcheck-task-bunch'),
         current_timestamp, '0 0 * * * ?' FROM dual
  WHERE NOT EXISTS (SELECT 1 FROM rhnTaskoSchedule WHERE job_label = 'diskcheck-task-queue-default');
