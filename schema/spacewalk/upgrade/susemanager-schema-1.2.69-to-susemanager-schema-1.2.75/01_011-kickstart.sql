--
-- Copyright (c) 2011 Novell
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
--

update rhnactiontype set name = 'Initiate an auto installation' where label = 'kickstart.initiate';
update rhnactiontype set name = 'Schedule a package sync for auto installations' where label = 'kickstart.schedule_sync';
update rhnactiontype set name = 'SUSE Manager Network Daemon Configuration' where label = 'rhnsd.configure';
update rhnactiontype set name = 'Initiate an auto installation for a virtual guest.' where label = 'kickstart_guest.initiate';

commit;

