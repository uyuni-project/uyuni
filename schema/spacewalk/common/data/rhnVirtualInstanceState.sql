--
-- Copyright (c) 2008--2010 Red Hat, Inc.
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
-- Red Hat trademarks are not licensed under GPLv2. No permission is
-- granted to use or replicate Red Hat trademarks that are incorporated
-- in this software or its documentation. 
--
-- data for rhnVirtualInstanceState

insert into rhnVirtualInstanceState (id, name, label) values (sequence_nextval('rhn_vis_id_seq'), 'Unknown', 'unknown');
insert into rhnVirtualInstanceState (id, name, label) values (sequence_nextval('rhn_vis_id_seq'), 'Running', 'running');
insert into rhnVirtualInstanceState (id, name, label) values (sequence_nextval('rhn_vis_id_seq'), 'Stopped', 'stopped');
insert into rhnVirtualInstanceState (id, name, label) values (sequence_nextval('rhn_vis_id_seq'), 'Crashed', 'crashed');
insert into rhnVirtualInstanceState (id, name, label) values (sequence_nextval('rhn_vis_id_seq'), 'Paused', 'paused');
insert into rhnVirtualInstanceState (id, name, label) values (sequence_nextval('rhn_vis_id_seq'), 'Powering On', 'powering_on');
insert into rhnVirtualInstanceState (id, name, label) values (sequence_nextval('rhn_vis_id_seq'), 'Shutting Down', 'shutting_down');
insert into rhnVirtualInstanceState (id, name, label) values (sequence_nextval('rhn_vis_id_seq'), 'Powering Off', 'powering_off');
insert into rhnVirtualInstanceState (id, name, label) values (sequence_nextval('rhn_vis_id_seq'), 'Pausing', 'pausing');
insert into rhnVirtualInstanceState (id, name, label) values (sequence_nextval('rhn_vis_id_seq'), 'Suspending', 'suspending');
insert into rhnVirtualInstanceState (id, name, label) values (sequence_nextval('rhn_vis_id_seq'), 'Suspended', 'suspended');
insert into rhnVirtualInstanceState (id, name, label) values (sequence_nextval('rhn_vis_id_seq'), 'Resuming', 'resuming');
insert into rhnVirtualInstanceState (id, name, label) values (sequence_nextval('rhn_vis_id_seq'), 'Resetting', 'resetting');
insert into rhnVirtualInstanceState (id, name, label) values (sequence_nextval('rhn_vis_id_seq'), 'Migrating', 'migrating');

