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

insert into suseNotificationMessageType(id, label, name, priority) values (
	sequence_nextval('suse_notifmesstype_id_seq'), 'info',
	'A general notification message', 0);

insert into suseNotificationMessageType(id, label, name, priority) values (
	sequence_nextval('suse_notifmesstype_id_seq'), 'warning',
	'A general warning message', 1);

insert into suseNotificationMessageType(id, label, name, priority) values (
	sequence_nextval('suse_notifmesstype_id_seq'), 'error',
	'A general error message', 2);

commit;
