--
-- Copyright (c) 2013 SUSE
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--

insert into suseServerContactMethod (id, label, name, rank) values
	(0, 'default', 'server.contact-method.default', 0);
insert into suseServerContactMethod (id, label, name, rank) values
	(1, 'ssh-push', 'server.contact-method.ssh-push', 10);
insert into suseServerContactMethod (id, label, name, rank) values
	(2, 'ssh-push-tunnel', 'server.contact-method.ssh-push-tunnel', 20);

