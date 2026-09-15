--
-- Copyright (c) 2025 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- SPDX-License-Identifier: GPL-2.0-only


INSERT INTO rhnTaskoBunch (id, name, description, org_bunch)
SELECT sequence_nextval('rhn_tasko_bunch_id_seq'), 'root-ca-cert-update-bunch', 'Updates root ca certificates', null FROM dual
WHERE NOT EXISTS (SELECT 1 FROM rhnTaskoBunch WHERE name = 'root-ca-cert-update-bunch');

INSERT INTO rhnTaskoTask (id, name, class)
SELECT sequence_nextval('rhn_tasko_task_id_seq'), 'root-ca-cert-update', 'com.redhat.rhn.taskomatic.task.RootCaCertUpdateTask' FROM dual
WHERE NOT EXISTS (SELECT 1 FROM rhnTaskoTask WHERE name = 'root-ca-cert-update');


INSERT INTO rhnTaskoTemplate (id, bunch_id, task_id, ordering, start_if)
SELECT sequence_nextval('rhn_tasko_template_id_seq'),
    (SELECT id FROM rhnTaskoBunch WHERE name='root-ca-cert-update-bunch'),
    (SELECT id FROM rhnTaskoTask WHERE name='root-ca-cert-update'),
    0,
    null FROM dual
WHERE NOT EXISTS (SELECT 1 FROM rhnTaskoTemplate
                                WHERE bunch_id = (SELECT id FROM rhnTaskoBunch WHERE name='root-ca-cert-update-bunch')
                                AND task_id = (SELECT id FROM rhnTaskoTask WHERE name='root-ca-cert-update'));
