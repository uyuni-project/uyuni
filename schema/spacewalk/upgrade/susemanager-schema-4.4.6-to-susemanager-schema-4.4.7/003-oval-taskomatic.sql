--
-- Copyright (c) 2023 SUSE LLC
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
SELECT sequence_nextval('rhn_tasko_task_id_seq'), 'oval-data-sync', 'com.redhat.rhn.taskomatic.task.OVALDataSync'
WHERE NOT EXISTS (SELECT 1
                  FROM rhnTaskoTask
                  WHERE name = 'oval-data-sync');

-- Insert bunch
INSERT INTO rhnTaskoBunch (id, name, description, org_bunch)
SELECT sequence_nextval('rhn_tasko_bunch_id_seq'),
       'oval-data-sync-bunch',
       'Generate OVAL data required to increase the accuracy of CVE audit queries.',
       null
WHERE NOT EXISTS (SELECT 1
                  FROM rhnTaskoBunch
                  WHERE name = 'oval-data-sync-bunch');

-- Insert template
INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
SELECT sequence_nextval('rhn_tasko_template_id_seq'),
       (SELECT id FROM rhnTaskoBunch WHERE name = 'oval-data-sync-bunch'),
       (SELECT id FROM rhnTaskoTask WHERE name = 'oval-data-sync'),
       0,
       null
WHERE NOT EXISTS (SELECT 1
                  FROM rhnTaskoTemplate
                  WHERE bunch_id = (SELECT id FROM rhnTaskoBunch WHERE name = 'oval-data-sync-bunch')
                    AND task_id = (SELECT id FROM rhnTaskoTask WHERE name = 'oval-data-sync')
                    AND ordering = 0);

-- Insert schedule for task (once a day at 11:00 PM)
INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
SELECT sequence_nextval('rhn_tasko_schedule_id_seq'),
       'oval-data-sync-default',
       (SELECT id FROM rhnTaskoBunch WHERE name = 'oval-data-sync-bunch'),
       current_timestamp,
       '0 0 23 ? * *'
WHERE NOT EXISTS (SELECT 1
                  FROM rhnTaskoSchedule
                  WHERE job_label = 'oval-data-sync-default');