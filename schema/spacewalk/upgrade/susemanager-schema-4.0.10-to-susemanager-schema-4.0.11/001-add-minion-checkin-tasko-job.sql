-- Copyright (c) 2019 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

INSERT INTO rhnTaskoBunch (id, name, description, org_bunch)
    SELECT sequence_nextval('rhn_tasko_bunch_id_seq'), 'minion-checkin-bunch', 'Perform a regular check-in on minions', null
    FROM dual WHERE NOT EXISTS (
        SELECT 1 FROM rhnTaskoBunch WHERE
        name='minion-checkin-bunch'
    );

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    SELECT sequence_nextval('rhn_tasko_schedule_id_seq'), 'minion-checkin-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='minion-checkin-bunch'),
        current_timestamp, '0 0 * * * ?'
    FROM dual WHERE NOT EXISTS (
        SELECT 1 FROM rhnTaskoSchedule WHERE
        job_label='minion-checkin-default'
    );

INSERT INTO rhnTaskoTask (id, name, class)
    SELECT sequence_nextval('rhn_tasko_task_id_seq'), 'minion-checkin', 'com.redhat.rhn.taskomatic.task.MinionCheckin'
    FROM dual WHERE NOT EXISTS (
        SELECT 1 FROM rhnTaskoTask WHERE
        name='minion-checkin'
    );

INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
    SELECT sequence_nextval('rhn_tasko_template_id_seq'),
                        (SELECT id FROM rhnTaskoBunch WHERE name='minion-checkin-bunch'),
                        (SELECT id FROM rhnTaskoTask WHERE name='minion-checkin'),
                        0,
                        null
    FROM dual WHERE NOT EXISTS (
        SELECT 1 FROM rhnTaskoTemplate WHERE
        bunch_id=(SELECT id FROM rhnTaskoBunch WHERE name='minion-checkin-bunch') AND
        task_id=(SELECT id FROM rhnTaskoTask WHERE name='minion-checkin')
    );
