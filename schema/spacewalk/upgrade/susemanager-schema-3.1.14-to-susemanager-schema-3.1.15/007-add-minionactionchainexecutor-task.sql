--
-- Copyright (c) 2018 SUSE LLC
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

INSERT INTO rhnTaskoBunch (id, name, description, org_bunch)
    VALUES (sequence_nextval('rhn_tasko_bunch_id_seq'), 'minion-action-chain-executor-bunch', 'Execute action chains on Minions', null);

INSERT INTO rhnTaskoTask (id, name, class)
    VALUES (sequence_nextval('rhn_tasko_task_id_seq'), 'minion-action-chain-executor', 'com.redhat.rhn.taskomatic.task.MinionActionChainExecutor');

INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
    VALUES (sequence_nextval('rhn_tasko_template_id_seq'),
        (SELECT id FROM rhnTaskoBunch WHERE name='minion-action-chain-executor-bunch'),
        (SELECT id FROM rhnTaskoTask WHERE name='minion-action-chain-executor'),
        0,
        null);
