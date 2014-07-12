--
-- Copyright (c) 2008--2013 Red Hat, Inc.
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
--
--
--

insert into rhnChannelPermissionRole (id, label, description)
	values (sequence_nextval('rhn_cperm_role_id_seq'),
		'subscribe',
		'Permission to subscribe to channel');
insert into rhnChannelPermissionRole (id, label, description)
	values (sequence_nextval('rhn_cperm_role_id_seq'),
		'manage',
		'Permission to manage channel');

