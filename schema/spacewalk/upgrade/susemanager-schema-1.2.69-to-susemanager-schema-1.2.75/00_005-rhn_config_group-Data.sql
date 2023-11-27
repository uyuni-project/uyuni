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

update rhn_config_group set description = 'SUSE Manager time synchronization' where name = 'timesync' ;
update rhn_config_group set description = 'General satellite configuration' where name = 'satellite' ;
update rhn_config_group set description = 'SUSE Manager dequeuer configuration' where name = 'queues' ;
update rhn_config_group set description = 'SUSE Manager configurator configuration' where name = 'ConfigPusher' ;



