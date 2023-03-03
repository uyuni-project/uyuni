--
-- Copyright (c) 2010--2016 Red Hat, Inc.
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation.
--

-- Top of every minute
INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES(sequence_nextval('rhn_tasko_schedule_id_seq'), 'errata-queue-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='errata-queue-bunch'),
        current_timestamp, '0 * * * * ?');

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES(sequence_nextval('rhn_tasko_schedule_id_seq'), 'cobbler-sync-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='cobbler-sync-bunch'),
        current_timestamp, '0 * * * * ?');

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES(sequence_nextval('rhn_tasko_schedule_id_seq'), 'channel-repodata-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='channel-repodata-bunch'),
        current_timestamp, '0 * * * * ?');

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES(sequence_nextval('rhn_tasko_schedule_id_seq'), 'errata-cache-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='errata-cache-bunch'),
        current_timestamp, '0 * * * * ?');

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES (sequence_nextval('rhn_tasko_schedule_id_seq'), 'ssh-push-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='ssh-push-bunch'),
        current_timestamp, '0 * * * * ?');

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES(sequence_nextval('rhn_tasko_schedule_id_seq'), 'system-overview-update-queue-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='system-overview-update-queue-bunch'),
        current_timestamp, '0 * * * * ?');

-- Every 10 minutes

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES(sequence_nextval('rhn_tasko_schedule_id_seq'), 'package-cleanup-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='package-cleanup-bunch'),
        current_timestamp, '0 0/10 * * * ?');

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES(sequence_nextval('rhn_tasko_schedule_id_seq'), 'kickstart-cleanup-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='kickstart-cleanup-bunch'),
        current_timestamp, '0 0/10 * * * ?');

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES(sequence_nextval('rhn_tasko_schedule_id_seq'), 'kickstartfile-sync-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='kickstartfile-sync-bunch'),
        current_timestamp, '0 0/10 * * * ?');

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES(sequence_nextval('rhn_tasko_schedule_id_seq'), 'auto-errata-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='auto-errata-bunch'),
        current_timestamp, '0 5/10 * * * ?');

-- Every 15 minutes

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES(sequence_nextval('rhn_tasko_schedule_id_seq'), 'session-cleanup-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='session-cleanup-bunch'),
        current_timestamp, '0 0/15 * * * ?');

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES(sequence_nextval('rhn_tasko_schedule_id_seq'), 'mgr-forward-registration-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='mgr-forward-registration-bunch'),
        current_timestamp, '0 0/15 * * * ?');

-- Every hour

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES(sequence_nextval('rhn_tasko_schedule_id_seq'), 'reboot-action-cleanup-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='reboot-action-cleanup-bunch'),
        current_timestamp, '0 0 * * * ?');

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES (sequence_nextval('rhn_tasko_schedule_id_seq'), 'minion-checkin-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='minion-checkin-bunch'),
        current_timestamp, '0 0 * * * ?');

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES (sequence_nextval('rhn_tasko_schedule_id_seq'), 'minion-action-cleanup-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='minion-action-cleanup-bunch'),
        current_timestamp, '0 0 0 * * ?');

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
VALUES (sequence_nextval('rhn_tasko_schedule_id_seq'), 'update-system-overview-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='update-system-overview-bunch'),
        current_timestamp, '0 0 * * * ?');

-- Once a day at 4:05:00 AM (beware of 2AM cronjobs)

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES(sequence_nextval('rhn_tasko_schedule_id_seq'), 'sandbox-cleanup-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='sandbox-cleanup-bunch'),
        current_timestamp, '0 5 4 ? * *');

-- Once a day at 11:00 PM

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES(sequence_nextval('rhn_tasko_schedule_id_seq'), 'daily-status-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='daily-status-bunch'),
        current_timestamp, '0 0 23 ? * *');

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES(sequence_nextval('rhn_tasko_schedule_id_seq'), 'compare-configs-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='compare-configs-bunch'),
        current_timestamp, '0 0 23 ? * *');

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES(sequence_nextval('rhn_tasko_schedule_id_seq'), 'clear-taskologs-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='clear-taskologs-bunch'),
        current_timestamp, '0 0 23 ? * *');

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES(sequence_nextval('rhn_tasko_schedule_id_seq'), 'cleanup-data-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='cleanup-data-bunch'),
        current_timestamp, '0 0 23 ? * *');

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES (sequence_nextval('rhn_tasko_schedule_id_seq'), 'cve-server-channels-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='cve-server-channels-bunch'),
        current_timestamp, '0 0 23 ? * *');

-- Once a day at 00:00

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES (sequence_nextval('rhn_tasko_schedule_id_seq'), 'mgr-sync-refresh-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='mgr-sync-refresh-bunch'),
        current_timestamp, '0 0 0 ? * *');

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES (sequence_nextval('rhn_tasko_schedule_id_seq'), 'gatherer-matcher-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='gatherer-matcher-bunch'),
        current_timestamp, '0 0 0 ? * *');

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES(sequence_nextval('rhn_tasko_schedule_id_seq'), 'uuid-cleanup-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='uuid-cleanup-bunch'),
        current_timestamp, '0 0 * * * ?');

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES (sequence_nextval('rhn_tasko_schedule_id_seq'), 'token-cleanup-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='token-cleanup-bunch'),
        current_timestamp, '0 0 0 ? * *');

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES (sequence_nextval('rhn_tasko_schedule_id_seq'), 'minion-action-chain-cleanup-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='minion-action-chain-cleanup-bunch'),
        current_timestamp, '0 0 * * * ?');

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES (sequence_nextval('rhn_tasko_schedule_id_seq'), 'notifications-cleanup-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='notifications-cleanup-bunch'),
        current_timestamp, '0 0 0 ? * *');

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
VALUES(sequence_nextval('rhn_tasko_schedule_id_seq'), 'update-payg-default',
       (SELECT id FROM rhnTaskoBunch WHERE name='update-payg-data-bunch'),
       current_timestamp, '0 0/10 * * * ?');

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
    VALUES (sequence_nextval('rhn_tasko_schedule_id_seq'), 'update-reporting-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='mgr-update-reporting-bunch'),
        current_timestamp, '0 0 0 ? * *');

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
VALUES (sequence_nextval('rhn_tasko_schedule_id_seq'), 'update-reporting-hub-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='mgr-update-reporting-hub-bunch'),
        current_timestamp, '0 30 1 ? * *');

-- Once a month at the 15th at 5am

INSERT INTO rhnTaskoSchedule (id, job_label, bunch_id, active_from, cron_expr)
VALUES (sequence_nextval('rhn_tasko_schedule_id_seq'), 'system-profile-refresh-default',
        (SELECT id FROM rhnTaskoBunch WHERE name='system-profile-refresh-bunch'),
        current_timestamp, '0 0 5 15 * ?');

commit;
