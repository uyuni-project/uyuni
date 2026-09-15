--
-- Copyright (c) 2014 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- SPDX-License-Identifier: GPL-2.0-only
--

-- Insert task and map to the Java class
INSERT INTO rhnTaskoTask (id, name, class)
    VALUES (sequence_nextval('rhn_tasko_task_id_seq'), 'mgr-sync-refresh',
        'com.redhat.rhn.taskomatic.task.MgrSyncRefresh');

-- Insert bunch
INSERT INTO rhnTaskoBunch (id, name, description, org_bunch)
    VALUES (sequence_nextval('rhn_tasko_bunch_id_seq'), 'mgr-sync-refresh-bunch',
        'Refresh data about channels, products and subscriptions', null);

-- Insert template
INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
    VALUES (sequence_nextval('rhn_tasko_template_id_seq'),
        (SELECT id FROM rhnTaskoBunch WHERE name='mgr-sync-refresh-bunch'),
        (SELECT id FROM rhnTaskoTask WHERE name='mgr-sync-refresh'),
        0, null);

-- Insert schedule for task (once a day at 11:00 PM)
INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES (sequence_nextval('rhn_tasko_schedule_id_seq'), 'mgr-sync-refresh-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='mgr-sync-refresh-bunch'),
        current_timestamp, '0 0 0 ? * *');
