/*
 * Copyright (c) 2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

DELETE FROM rhntaskorun
      WHERE template_id = (
            SELECT id
              FROM rhnTaskoTemplate
             WHERE bunch_id = (SELECT id FROM rhnTaskoBunch WHERE name='minion-action-chain-cleanup-bunch')
                        AND task_id = (SELECT id FROM rhnTaskoTask WHERE name='minion-action-chain-cleanup')
      );

DELETE FROM rhnTaskoTemplate
      WHERE bunch_id = (SELECT id FROM rhnTaskoBunch WHERE name='minion-action-chain-cleanup-bunch')
                        AND task_id = (SELECT id FROM rhnTaskoTask WHERE name='minion-action-chain-cleanup');

DELETE FROM rhnTaskoSchedule
      WHERE job_label = 'minion-action-chain-cleanup-default'
                        AND bunch_id = (SELECT id FROM rhnTaskoBunch WHERE name='minion-action-chain-cleanup-bunch');

DELETE FROM rhnTaskoTask
      WHERE name = 'minion-action-chain-cleanup'
                        AND class = 'com.redhat.rhn.taskomatic.task.MinionActionChainCleanup';

DELETE FROM rhnTaskoBunch
      WHERE name = 'minion-action-chain-cleanup-bunch';
