-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

UPDATE rhnTaskoBunch SET name = 'ssh-service-bunch' WHERE name = 'ssh-push-bunch';
UPDATE rhnTaskoSchedule SET job_label = 'ssh-service-default' WHERE job_label = 'ssh-push-default';
UPDATE rhnTaskoTask SET name = 'ssh-service', class = 'com.redhat.rhn.taskomatic.task.SSHService' WHERE name = 'ssh-push';

