--
-- Copyright (c) 2011 Novell
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--

INSERT INTO rhnTaskoBunch (id, name, description, org_bunch)
             VALUES (sequence_nextval('rhn_tasko_bunch_id_seq'), 'image-download-bunch', 'Download images', null);

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES(sequence_nextval('rhn_tasko_schedule_id_seq'), 'image-download-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='image-download-bunch'),
        current_timestamp, '0 * * * * ?');

INSERT INTO rhnTaskoTask (id, name, class)
         VALUES (sequence_nextval('rhn_tasko_task_id_seq'), 'image-download', 'com.redhat.rhn.taskomatic.task.ImageDownloadTask');

INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
             VALUES (sequence_nextval('rhn_tasko_template_id_seq'),
                        (SELECT id FROM rhnTaskoBunch WHERE name='image-download-bunch'),
                        (SELECT id FROM rhnTaskoTask WHERE name='image-download'),
                        0,
                        null);

commit;
