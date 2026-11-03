-- SPDX-FileCopyrightText: 2026 SUSE LLC
--
-- SPDX-License-Identifier: GPL-2.0-only

INSERT INTO rhnTaskoTask (id, name, class)
             VALUES (sequence_nextval('rhn_tasko_task_id_seq'), 'cleanup-packagechangelog-data', 'com.redhat.rhn.taskomatic.task.ChangeLogCleanUp');
